terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.44.0"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region = "us-west-2"
}

resource "aws_vpc" "my_vpc" {
  cidr_block = "10.0.0.0/16"
}

resource "aws_subnet" "my_subnet_az1" {
  vpc_id                  = aws_vpc.my_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "us-west-2a"
  map_public_ip_on_launch = true
}

resource "aws_subnet" "my_subnet_az2" {
  vpc_id                  = aws_vpc.my_vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "us-west-2b"
  map_public_ip_on_launch = true
}

resource "aws_internet_gateway" "my_gateway" {
  vpc_id = aws_vpc.my_vpc.id
}

resource "aws_route_table" "my_route_table" {
  vpc_id = aws_vpc.my_vpc.id
}

resource "aws_route_table_association" "my_route_table_association" {
  subnet_id      = aws_subnet.my_subnet_az1.id
  route_table_id = aws_route_table.my_route_table.id
}

resource "aws_route" "internet_access" {
  route_table_id         = aws_route_table.my_route_table.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.my_gateway.id
}

resource "aws_security_group" "my_security_group" {
  vpc_id = aws_vpc.my_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "my_rds_security_group" {
  vpc_id = aws_vpc.my_vpc.id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    security_groups = [aws_security_group.my_security_group.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_subnet_group" "my_db_subnet_group" {
  name       = "my-db-subnet-group"
  subnet_ids = [
    aws_subnet.my_subnet_az1.id,
    aws_subnet.my_subnet_az2.id
  ]

  tags = {
    Name = "MyDBSubnetGroup"
  }
}

resource "aws_db_instance" "my_rds_instance" {
  identifier        = "slovo-db"
  engine            = "postgres"
  engine_version    = "12.16"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  db_name           = "slovoDB"
  username          = "slovo"
  password          = "slovo-complicated-password"
  port              = 5432
  db_subnet_group_name = aws_db_subnet_group.my_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.my_rds_security_group.id]

  multi_az          = false
  publicly_accessible = false

  tags = {
    Name = "SlovoDB"
  }

  lifecycle {
    create_before_destroy = true
  }

  skip_final_snapshot = true
}

resource "tls_private_key" "my_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "my_key_pair" {
  key_name   = "my-key-pair"
  public_key = tls_private_key.my_key.public_key_openssh
}

data "template_file" "sql_script" {
  template = file("database.sql")
}

data "template_file" "start_script" {
  template = file("start.sh")

  vars = {
    SQL_SCRIPT_CONTENT = data.template_file.sql_script.rendered
    DB_PASSWORD        = aws_db_instance.my_rds_instance.password
    DB_ENDPOINT        = aws_db_instance.my_rds_instance.address
    DB_USERNAME        = aws_db_instance.my_rds_instance.username
    DB_NAME            = aws_db_instance.my_rds_instance.db_name
  }
}

resource "aws_instance" "my_instance" {
  ami           = "ami-08116b9957a259459"
  instance_type = "t2.micro"
  subnet_id     = aws_subnet.my_subnet_az1.id
  key_name      = aws_key_pair.my_key_pair.key_name
  vpc_security_group_ids = [aws_security_group.my_security_group.id]

  user_data = data.template_file.start_script.rendered

  tags = {
    Name = "Instance"
  }

  lifecycle {
    create_before_destroy = true
  }
}


output "instance_public_ip" {
  value = aws_instance.my_instance.public_ip
}

output "private_key" {
  value     = tls_private_key.my_key.private_key_pem
  sensitive = true
}

output "db_address" {
  value = aws_db_instance.my_rds_instance.address
}

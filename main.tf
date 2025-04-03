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

resource "aws_subnet" "my_subnet" {
  vpc_id                  = aws_vpc.my_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "us-west-2a"
  map_public_ip_on_launch = true
}

resource "aws_internet_gateway" "my_gateway" {
  vpc_id = aws_vpc.my_vpc.id
}

resource "aws_route_table" "my_route_table" {
  vpc_id = aws_vpc.my_vpc.id
}

resource "aws_route_table_association" "my_route_table_association" {
  subnet_id      = aws_subnet.my_subnet.id
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

resource "tls_private_key" "my_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "my_key_pair" {
  key_name   = "my-key-pair"
  public_key = tls_private_key.my_key.public_key_openssh
}

resource "aws_instance" "my_instance" {
  ami           = "ami-08116b9957a259459"
  instance_type = "t2.micro"
  subnet_id     = aws_subnet.my_subnet.id
  key_name      = aws_key_pair.my_key_pair.key_name

  vpc_security_group_ids = [aws_security_group.my_security_group.id]

  tags = {
    Name = "MyEC2Instance"
  }
}

output "instance_public_ip" {
  value = aws_instance.my_instance.public_ip
}

output "private_key" {
  value     = tls_private_key.my_key.private_key_pem
  sensitive = true
}

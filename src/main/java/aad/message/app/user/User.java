    package aad.message.app.user;

    import jakarta.persistence.Entity;
    import jakarta.persistence.Table;
    import jakarta.persistence.Id;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Column;

    @Entity
    @Table(name = "\"user\"")
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "username")
        private String username;

        @Column(name = "password")
        private String password;

        @Column(name = "first_name")
        private String firstName;

        @Column(name = "last_name")
        private String lastName;

        @Column(name = "email")
        private String email;

        @Column(name = "image_url")
        private String imageUrl;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String first_name) {
            this.firstName = first_name;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String last_name) {
            this.lastName = last_name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String image_url) {
            this.imageUrl = image_url;
        }
    }

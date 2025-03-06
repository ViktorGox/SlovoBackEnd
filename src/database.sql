CREATE TABLE "user" (
                        id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        first_name TEXT NOT NULL,
                        last_name TEXT,
                        email TEXT UNIQUE NOT NULL,
                        image_url TEXT
);

CREATE TABLE "group" (
                         id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name TEXT NOT NULL,
                         image_url TEXT,
                         reminder_start TIMESTAMP,
                         reminder_frequency INTERVAL
);

CREATE TABLE role (
                      id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      name TEXT NOT NULL
);

CREATE TABLE group_user_role (
                                 group_id INTEGER NOT NULL,
                                 user_id INTEGER NOT NULL,
                                 role_id INTEGER NOT NULL,
                                 PRIMARY KEY (group_id, user_id, role_id),
                                 FOREIGN KEY (group_id) REFERENCES "group"(id),
                                 FOREIGN KEY (user_id) REFERENCES "user"(id),
                                 FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE message_text (
                              id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              text TEXT NOT NULL,
                              reply_message_id INTEGER,
                              user_id INTEGER NOT NULL,
                              group_id INTEGER NOT NULL,
                              sent_date TIMESTAMP NOT NULL,
                              FOREIGN KEY (reply_message_id) REFERENCES message_text(id),
                              FOREIGN KEY (user_id) REFERENCES "user"(id),
                              FOREIGN KEY (group_id) REFERENCES "group"(id)
);

CREATE TABLE message_audio (
                               id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               audio_url TEXT NOT NULL,
                               transcription TEXT,
                               reply_message_id INTEGER,
                               user_id INTEGER NOT NULL,
                               sent_date TIMESTAMP NOT NULL,
                               FOREIGN KEY (reply_message_id) REFERENCES message_audio(id),
                               FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE message_audio_group (
                                     message_audio_id INTEGER NOT NULL,
                                     group_id INTEGER NOT NULL,
                                     PRIMARY KEY (message_audio_id, group_id),
                                     FOREIGN KEY (message_audio_id) REFERENCES message_audio(id),
                                     FOREIGN KEY (group_id) REFERENCES "group"(id)
);

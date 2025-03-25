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
                         reminder_frequency INTEGER
);

CREATE TABLE role (
                      id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      name TEXT NOT NULL
);

CREATE TABLE group_user_role (
                                 id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 group_id INTEGER NOT NULL,
                                 user_id INTEGER NOT NULL,
                                 role_id INTEGER NOT NULL,
                                 FOREIGN KEY (group_id) REFERENCES "group"(id),
                                 FOREIGN KEY (user_id) REFERENCES "user"(id),
                                 FOREIGN KEY (role_id) REFERENCES role(id)
);


CREATE TABLE message (
                         id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         user_id INTEGER NOT NULL,
                         sent_date TIMESTAMP WITH TIME ZONE NOT NULL,
                         message_type TEXT CHECK (message_type IN ('text', 'audio')),
                         reply_to_message_id INTEGER NULL REFERENCES message(id),
                         FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE message_text (
                              id INTEGER PRIMARY KEY REFERENCES message(id),
                              text TEXT NOT NULL,
                              group_id INTEGER NOT NULL,
                              FOREIGN KEY (group_id) REFERENCES "group"(id)
);

CREATE TABLE message_audio (
                               id INTEGER PRIMARY KEY REFERENCES message(id),
                               audio_url TEXT NOT NULL,
                               transcription TEXT NOT NULL
);

CREATE TABLE message_audio_group (
                                     message_audio_id INTEGER NOT NULL,
                                     group_id INTEGER NOT NULL,
                                     PRIMARY KEY (message_audio_id, group_id),
                                     FOREIGN KEY (message_audio_id) REFERENCES message_audio(id) ON DELETE CASCADE,
                                     FOREIGN KEY (group_id) REFERENCES "group"(id) ON DELETE CASCADE
);

INSERT INTO role (name) VALUES
                            ('User'),
                            ('Admin'),
                            ('Owner');

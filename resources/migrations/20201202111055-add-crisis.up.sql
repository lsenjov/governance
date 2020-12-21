CREATE TABLE crisis (
 id UUID PRIMARY KEY,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL,
 announcement text NOT NULL,
 description text NOT NULL
 );

--;;
CREATE TABLE tag (
 id UUID PRIMARY KEY,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL,
 name text NOT NULL
 );

--;;
-- tables in link names are always alphabetical
CREATE TABLE link_crisis_tag (
 crisis_id UUID NOT NULL REFERENCES crisis(id),
 tag_id UUID NOT NULL REFERENCES tag(id),
 PRIMARY KEY(crisis_id, tag_id)
 );
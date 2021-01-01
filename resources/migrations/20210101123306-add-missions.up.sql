CREATE TABLE departments (
 id UUID PRIMARY KEY,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL,
 name text NOT NULL
);

--;;
CREATE TABLE missions (
 id UUID PRIMARY KEY,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL,
 mission_text text NOT NULL,
 -- Every mission text absolutely has an attached department
 department_id UUID NOT NULL REFERENCES departments(id),
 -- But if it doesn't have an attached crisis, it's a generic mission
 crisis_id UUID REFERENCES crisis(id)
 );
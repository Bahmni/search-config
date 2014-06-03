CREATE OR REPLACE FUNCTION add_requestor_names(firstName TEXT, lastName TEXT, middleName TEXT DEFAULT null, externalId TEXT DEFAULT null) RETURNS VOID AS
$$
DECLARE
    personId INT;
BEGIN
    BEGIN
      BEGIN
        PERFORM personId FROM clinlims.person WHERE first_name = firstName and last_name = lastName;
        IF NOT FOUND THEN
          INSERT INTO clinlims.person(id, first_name, middle_name, last_name)
          VALUES (nextval('person_seq'), firstName, middleName, lastName);
        END IF;
      END;

      BEGIN
        SELECT id INTO personId FROM clinlims.person WHERE first_name = firstName and last_name = lastName;
        IF FOUND THEN
          PERFORM id FROM clinlims.provider WHERE person_id = personId;
          IF NOT FOUND THEN
            INSERT INTO clinlims.provider(id, person_id, external_id)
            VALUES (nextval('provider_seq'), personId, externalId);
          END IF;
        END IF;

        EXCEPTION
        WHEN unique_violation THEN
          RAISE NOTICE 'unique key violation. Please try again';
      END;
    END;
END
$$
LANGUAGE plpgsql;
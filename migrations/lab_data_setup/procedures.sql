-- =======================================================================================================
-- Connects a UnitOfMeasure to a TEST. Also inserts the UnitOfMeasure if it does not exist.
-- =======================================================================================================

CREATE OR REPLACE FUNCTION insert_unit_of_measure(unit_of_measure_name TEXT, test_name TEXT) RETURNS VOID AS
$$
DECLARE
    unit_id int;
    test_id_value int;
BEGIN
    
    BEGIN
        SELECT id INTO STRICT test_id_value FROM clinlims.test WHERE name = test_name or description = test_name;
        EXCEPTION   
                    WHEN NO_DATA_FOUND THEN
                    RAISE EXCEPTION 'test % not found', test_name;
                    WHEN TOO_MANY_ROWS THEN
                    RAISE EXCEPTION 'test % not unique', test_name;  
    END;  

    SELECT id INTO unit_id FROM clinlims.unit_of_measure WHERE name = unit_of_measure_name;
    IF NOT FOUND THEN

        INSERT INTO clinlims.unit_of_measure (id, name, description, lastupdated)
        VALUES (nextval('clinlims.unit_of_measure_seq'), unit_of_measure_name, unit_of_measure_name, localtimestamp);

        UPDATE clinlims.test SET uom_id = currval('clinlims.unit_of_measure_seq'), lastupdated = localtimestamp 
        WHERE id = test_id_value;

    ELSE

        UPDATE clinlims.test SET uom_id = unit_id, lastupdated = localtimestamp 
        WHERE id = test_id_value;    

    END IF;    

   
END
$$
LANGUAGE plpgsql;

-- =======================================================================================================
-- Adds a TEST Result Type of DICTIONARY with value as NORMAL
-- =======================================================================================================


CREATE OR REPLACE FUNCTION add_test_result_type_dictionary(test_name TEXT, test_result_value TEXT, lab_name TEXT) RETURNS VOID AS
$$
BEGIN
   PERFORM add_test_result_type_dictionary_with_normality(test_name, test_result_value, false, lab_name);
END
$$
LANGUAGE plpgsql;

-- Adds a TEST Result Type of DICTIONARY with value ABNORMAL

CREATE OR REPLACE FUNCTION add_test_result_type_dictionary_ABNORMAL(test_name TEXT, test_result_value TEXT, lab_name TEXT) RETURNS VOID AS
$$
BEGIN
   PERFORM add_test_result_type_dictionary_with_normality(test_name, test_result_value, true, lab_name);
END
$$
LANGUAGE plpgsql;

-- Adds a TEST Result Type of DICTIONARY with value normality

CREATE OR REPLACE FUNCTION add_test_result_type_dictionary_with_normality(test_name TEXT, test_result_value TEXT, is_abnormal BOOLEAN, lab_name TEXT) RETURNS VOID AS
$$
DECLARE
    test_id_value INT;
    test_result_id_value INT;
    dictionary_id_value INT;
    dictionary_categ_id_value INT;
BEGIN

    BEGIN
        SELECT id INTO STRICT test_id_value FROM clinlims.test WHERE name = test_name or description = test_name;
        EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                    RAISE EXCEPTION 'test % not found', test_name;
                    WHEN TOO_MANY_ROWS THEN
                    RAISE EXCEPTION 'test % not unique', test_name;
    END;

    BEGIN
        SELECT id INTO STRICT dictionary_categ_id_value FROM clinlims.dictionary_category WHERE name = lab_name;
        EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                    RAISE EXCEPTION 'Dictionary category % not found', lab_name;
                    WHEN TOO_MANY_ROWS THEN
                    RAISE EXCEPTION 'Dictionary category % is not unique', lab_name;
    END;

    BEGIN
        SELECT id INTO dictionary_id_value FROM clinlims.dictionary WHERE dict_entry = test_result_value and dictionary_category_id = dictionary_categ_id_value;
        IF NOT FOUND THEN
            INSERT INTO clinlims.dictionary(id, dict_entry, dictionary_category_id, lastupdated)
            VALUES (nextval('dictionary_seq'), test_result_value, dictionary_categ_id_value, localtimestamp);
        END IF;
    END;

    BEGIN
        SELECT id INTO STRICT dictionary_id_value FROM clinlims.dictionary WHERE dict_entry = test_result_value and dictionary_category_id = dictionary_categ_id_value;
        SELECT id INTO test_result_id_value FROM clinlims.test_result WHERE test_id = test_id_value and tst_rslt_type = 'D' and value = cast(dictionary_id_value as text);
        IF NOT FOUND THEN
            INSERT INTO clinlims.test_result(id, test_id, tst_rslt_type, value, abnormal)
            VALUES (nextval('clinlims.test_result_seq'), test_id_value, 'D', dictionary_id_value, is_abnormal);
        END IF;
    END;
END
$$
LANGUAGE plpgsql;

-- =======================================================================================================
-- Add Result Type as REMARKS to a TEST
-- =======================================================================================================


CREATE OR REPLACE FUNCTION add_test_result_type_remarks(test_name TEXT) RETURNS VOID AS
$$
DECLARE
    test_id_value INT;
    test_result_id_value INT;
BEGIN

    BEGIN
        SELECT id INTO STRICT test_id_value FROM clinlims.test WHERE name = test_name or description = test_name;
        EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                    RAISE EXCEPTION 'test % not found', test_name;
                    WHEN TOO_MANY_ROWS THEN
                    RAISE EXCEPTION 'test % not unique', test_name;
    END;

    BEGIN
        SELECT id INTO test_result_id_value FROM clinlims.test_result WHERE test_id = test_id_value;
        IF NOT FOUND THEN
            INSERT INTO clinlims.test_result(id, test_id, tst_rslt_type)
            VALUES (nextval('clinlims.test_result_seq'), test_id_value, 'R');
        END IF;
    END;
END
$$
LANGUAGE plpgsql;


-- ========================================================================================================================
-- Insert/UPDATE RESULT LIMIT NORMAL RANGE VALUES FOR NUMERIC TEST (for given gender+minAge+maxAge)
-- ========================================================================================================================


CREATE OR REPLACE FUNCTION insert_test_result_with_range(test_name TEXT, _gender TEXT, _min_age double precision, _max_age double precision, lower_limit double precision, upper_limit double precision, _low_valid double precision, _high_valid double precision) RETURNS VOID AS
$$
DECLARE
    result_limits_id int;
    test_id_value int;
    gender_value TEXT;
    min_age_value double precision;
    max_age_value double precision;

BEGIN

    IF lower_limit = -1 AND upper_limit = -1 THEN
        RETURN ;
    END IF;

    IF _gender is not null THEN
        gender_value = _gender;
    ELSE
        gender_value = NULL;
    END IF;    

    IF _min_age is not null THEN
        min_age_value = _min_age;
    ELSE
        min_age_value = NULL;
    END IF;    

    IF _max_age is not null THEN
        max_age_value = _max_age;
    ELSE
        max_age_value = NULL;
    END IF;    


    BEGIN
        SELECT id INTO STRICT test_id_value FROM clinlims.test WHERE name = test_name or description = test_name;
        EXCEPTION   
                    WHEN NO_DATA_FOUND THEN
                    RAISE EXCEPTION 'test % not found', test_name;
                    WHEN TOO_MANY_ROWS THEN
                    RAISE EXCEPTION 'test % not unique', test_name;  
    END;  

    SELECT id INTO result_limits_id FROM clinlims.result_limits WHERE test_id = test_id_value AND gender = gender_value AND min_age = min_age_value AND max_age = max_age_value;
    IF NOT FOUND THEN

        INSERT INTO clinlims.result_limits (id, test_id, test_result_type_id, gender, min_age, max_age, low_normal, high_normal, low_valid, high_valid, lastupdated)
        VALUES (nextval('clinlims.result_limits_seq'), test_id_value, 4, gender_value, min_age_value, max_age_value,lower_limit, upper_limit, _low_valid, _high_valid,localtimestamp);

    ELSE

        UPDATE clinlims.result_limits SET low_normal = lower_limit, high_normal = upper_limit, low_valid = _low_valid, high_valid = _high_valid, lastupdated = localtimestamp 
        WHERE id = result_limits_id;

    END IF;    

   
END
$$
LANGUAGE plpgsql;

-- ========================================================================================================================
-- Insert/UPDATE RESULT LIMIT NORMAL RANGE VALUES FOR NUMERIC TEST (ignore Gender and ignore Age)
-- ========================================================================================================================

CREATE OR REPLACE FUNCTION insert_test_result_with_range(test_name TEXT, lower_limit double precision, upper_limit double precision, _low_valid double precision default '-infinity', _high_valid double precision default 'infinity') RETURNS VOID AS
$$
BEGIN
    
    PERFORM insert_test_result_with_range(test_name, NULL, 0, 'infinity', lower_limit, upper_limit, _low_valid, _high_valid);
   
END
$$
LANGUAGE plpgsql;


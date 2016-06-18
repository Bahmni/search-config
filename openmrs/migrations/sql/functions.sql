DROP FUNCTION IF EXISTS isHTInControl;
CREATE FUNCTION isHTInControl(pStartDate DATE, pEndDate DATE, pPersonId INT) RETURNS INT
DETERMINISTIC
  BEGIN
    DECLARE systolic INT;
    DECLARE diastolic INT;
    DECLARE counter INT;
    DECLARE cvalsys INT;
    DECLARE cvaldia INT;
    DECLARE dgosis TEXT;
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE curs CURSOR FOR
      select obs.concept_id,value_numeric,group_concat(cdvn.name) cname,max(date_created) from obs
        LEFT JOIN confirmed_diagnosis_view_new cdvn on cdvn.person_id=obs.person_id
      where obs.concept_id in
            (systolic,diastolic) and obs.person_id=pPersonId
            and date(date_created) BETWEEN pStartDate and pEndDate
      GROUP BY obs.concept_id,obs.person_id
      ORDER BY  obs.encounter_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;

    SELECT concept_id into systolic from concept_name where name='Systolic'  and
                                                            concept_name_type='FULLY_SPECIFIED';
    SELECT concept_id into diastolic from concept_name where name='Diastolic' and
                                              concept_name_type='FULLY_SPECIFIED';

    select group_concat(cdvn.name) cname INTO dgosis
    from confirmed_diagnosis_view_new cdvn where cdvn.person_id=pPersonId;

    select value_numeric into cvalsys from obs where obs.concept_id in (systolic) and obs.person_id=pPersonId
          and date(date_created) BETWEEN pStartDate and pEndDate ORDER BY obs.obs_id desc LIMIT 1;
    select value_numeric into cvaldia from obs where obs.concept_id in (diastolic) and obs.person_id=pPersonId
          and date(date_created) BETWEEN pStartDate and pEndDate ORDER BY obs.obs_id desc LIMIT 1;
    SET counter = 0;

    IF cvalsys is NULL  or cvaldia is NULL THEN
      RETURN (0);
    END IF;

    IF STRCMP(dgosis,'Diabetes') >= 0 OR STRCMP(dgosis,'Chronic Kidney Disease') >= 0 THEN
      IF cvalsys<130 AND cvaldia<80 THEN
        SET counter = counter + 1;
      END IF;
    ELSE
      IF cvalsys<140 AND cvaldia<90 THEN
        SET counter = counter + 1;
      END IF;
    END IF;
    RETURN (counter);
  END
$$

DROP FUNCTION IF EXISTS isDiabetesInControl;
CREATE FUNCTION isDiabetesInControl(pStartDate DATE, pEndDate DATE, pPersonId INT) RETURNS INT
DETERMINISTIC
  BEGIN
    DECLARE bsfasting INT;
    DECLARE bspostmeal INT;
    DECLARE counter INT;
    DECLARE cvalfast INT;
    DECLARE cvalmeal INT;
    DECLARE dgosis TEXT;
    DECLARE v_finished INTEGER DEFAULT 0;
    SELECT concept_id into bsfasting from concept_name where name='Blood Sugar (Fasting)'  and
                                                            concept_name_type='FULLY_SPECIFIED';
    SELECT concept_id into bspostmeal from concept_name where name='Blood Sugar (Post Meal)' and
                                                             concept_name_type='FULLY_SPECIFIED';

    select value_numeric into cvalfast from obs where obs.concept_id in (bsfasting) and obs.person_id=pPersonId
      and obs.value_numeric is not null and date(date_created) BETWEEN pStartDate and pEndDate ORDER BY obs.obs_id desc LIMIT 1;
    select value_numeric into cvalmeal from obs where obs.concept_id in (bspostmeal) and obs.person_id=pPersonId
      and obs.value_numeric is not null and date(date_created) BETWEEN pStartDate and pEndDate ORDER BY obs.obs_id desc LIMIT 1;
    SET counter = 0;

    IF cvalfast is NULL  or cvalmeal is NULL THEN
      RETURN (0);
    END IF;

    IF cvalfast >79 AND cvalfast<126 THEN
      SET counter = counter + 1;
    END IF;
    IF cvalmeal>109 AND cvalmeal<181 THEN
      SET counter = counter + 1;
    END IF;
    IF counter=2 THEN
      RETURN 1;
    ELSE
      RETURN 0;
    END IF;
  END
$$
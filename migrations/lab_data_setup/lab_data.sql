SELECT add_test_result_type_remarks('P.S. for opinion /Anaemia');

-- Dictionary

SELECT add_test_result_type_dictionary('Platelet Adequacy', 'Adequate', 'Bahmni Lab'); 
SELECT add_test_result_type_dictionary_ABNORMAL('Platelet Adequacy', 'Increased', 'Bahmni Lab'); 
SELECT add_test_result_type_dictionary_ABNORMAL('Platelet Adequacy', 'Decreased', 'Bahmni Lab'); 

SELECT add_test_result_type_dictionary('P.S. for M.P.', '-ve', 'Bahmni Lab'); 
SELECT add_test_result_type_dictionary_ABNORMAL('P.S. for M.P.', 'PV', 'Bahmni Lab'); 
SELECT add_test_result_type_dictionary_ABNORMAL('P.S. for M.P.', 'PF', 'Bahmni Lab'); 
SELECT add_test_result_type_dictionary_ABNORMAL('P.S. for M.P.', 'PV+PF', 'Bahmni Lab'); 

SELECT add_test_result_type_dictionary('Sickling (Solubility)', '-ve', 'Bahmni Lab'); 
SELECT add_test_result_type_dictionary_ABNORMAL('Sickling (Solubility)', '+ve', 'Bahmni Lab'); 

SELECT add_test_result_type_dictionary('Blood group Rh type', 'A+ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'A-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'B+ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'B-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'AB+ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'AB-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'O+ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Blood group Rh type', 'O-ve', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Urine Albumin', 'Nil', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Albumin', 'Trace', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Albumin', '1+', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Albumin', '2+', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Albumin', '3+', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Albumin', '4+', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Urine Sugar', 'Nil', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Sugar', 'Trace', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Sugar', '0.5', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Sugar', '1', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Sugar', '1.5', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Sugar', '2', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Urine Urobilinogen', 'Normal', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Urobilinogen', 'Increased', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Urine Bilirubin', 'Absent', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Bilirubin', 'Present', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Urine Acetone / Ketone', 'Absent', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Urine Acetone / Ketone', 'Present', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('HIV I & II [Card Test]', 'Non reactive', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('HIV I & II [Card Test]', 'Reactive I', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('HIV I & II [Card Test]', 'Reactive II', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('HIV I & II [Card Test]', 'Reactive I and II', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('VDRL', 'Non reactive', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('VDRL', 'Reactive', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Widal', '-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary('Widal', '1:40', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Widal', '1:80', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Widal', '1:160', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Widal', '1:320', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('RA Test', '-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('RA Test', '+ve', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Malaria (Rapid Diagnosis Test)', '-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Malaria (Rapid Diagnosis Test)', '+ve (PF)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Malaria (Rapid Diagnosis Test)', '+ve (PV)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Malaria (Rapid Diagnosis Test)', '+ve (PF and PV)', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Hb-Electrophoresis', 'AA Pattern', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Hb-Electrophoresis', 'AS Pattern', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Hb-Electrophoresis', 'SS Pattern', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Sputum For AFB', '-ve', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Sputum For AFB', '+ve (scanty)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Sputum For AFB', '+ve (1+)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Sputum For AFB', '+ve (2+)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Sputum For AFB', '+ve (3+)', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Skin clipping for AFB', 'Negative', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Skin clipping for AFB', 'Positive (scanty)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Skin clipping for AFB', 'Positive (1+)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Skin clipping for AFB', 'Positive (2+)', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Skin clipping for AFB', 'Positive (3+)', 'Bahmni Lab');

SELECT add_test_result_type_dictionary('Cross-Matching(Blood Transfusion )', 'Compatible', 'Bahmni Lab');
SELECT add_test_result_type_dictionary_ABNORMAL('Cross-Matching(Blood Transfusion )', 'Non Compatible', 'Bahmni Lab');


-- Remarks

SELECT add_test_result_type_remarks('DLC (Differentiate Leukocytes Count)');
SELECT add_test_result_type_remarks('P.S. for opinion /Anaemia');
SELECT add_test_result_type_remarks('Urine Microscopy / Micro ');
SELECT add_test_result_type_remarks('Skin Scraping for Fungus ');
SELECT add_test_result_type_remarks('Semen Analysis ');
SELECT add_test_result_type_remarks('Stool RM');
SELECT add_test_result_type_remarks('PAP Smear');
SELECT add_test_result_type_remarks('FNAC');
SELECT add_test_result_type_remarks('Histopath');
SELECT add_test_result_type_remarks('TFT (T3, T4, TSH)');
SELECT add_test_result_type_remarks('Sr. Bilirubin (T+D+I)');


-- Numeric
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 0,0.999, 16,25, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 1,5, 11,14, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','F', 0,0.999, 16,25, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','F', 1,5, 11,14, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 6,10, 12,16, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','F', 6,'infinity', 12,16, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 11,'infinity', 12,18, '-infinity', 'infinity'); 
									 
SELECT insert_test_result_with_range('Hb% [Sahli''s]','M', 0,0.999, 16,25, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Hb% [Sahli''s]','F', 0,0.999, 16,25, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 1,5, 11,14, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','F', 1,5, 11,14, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 6,10, 12,16, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','F', 6,'infinity', 12,16, '-infinity', 'infinity'); 
SELECT insert_test_result_with_range('Hb% [Cynmeth]','M', 11,'infinity', 12,18, '-infinity', 'infinity'); 

SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','M', 0,0.999, 10000,25000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','F', 0,0.999, 10000,25000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','M', 1,3, 6000,18000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','F', 1,3, 6000,18000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','M', 4,7, 6000,15000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','F', 4,7, 6000,15000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','M', 8,12, 4500,13500, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','F', 8,12, 4500,13500, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','M', 13,'infinity', 4000,10000, '-infinity', 'infinity');
SELECT insert_test_result_with_range('TLC (Total Leukocytes Count)','F', 13,'infinity', 4000,10000, '-infinity', 'infinity');

SELECT insert_test_result_with_range('ESR','M', '-infinity','infinity', 0,15, '-infinity', 'infinity');
SELECT insert_test_result_with_range('ESR','F', '-infinity','infinity', 0,20, '-infinity', 'infinity');

SELECT insert_test_result_with_range('Total Cholesterol ','M', 0,0.077, 50, 170, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','M', 0.078,0.999, 60, 190, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','M', 1,13, 110, 230, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','M', 14,'infinity', 0, 200, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','F', 0,0.077, 50, 170, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','F', 0.078,0.999, 60, 190, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','F', 1,13, 110, 230, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Total Cholesterol ','F', 14,'infinity', 0, 200, '-infinity', 'infinity');

SELECT insert_test_result_with_range('Sr. Creatinine','M', '-infinity','infinity', 0.7, 1.4, '-infinity', 'infinity');
SELECT insert_test_result_with_range('Sr. Creatinine','F', '-infinity','infinity', 0.6, 1.2, '-infinity', 'infinity');

SELECT insert_test_result_with_range('SGOT','M', '0','4.99', 25, 95, '-infinity', 'infinity');
SELECT insert_test_result_with_range('SGOT','M', '5','infinity', 10, 40, '-infinity', 'infinity');
SELECT insert_test_result_with_range('SGOT','F', '0','4.99', 25, 95, '-infinity', 'infinity');
SELECT insert_test_result_with_range('SGOT','F', '5','infinity', 10, 40, '-infinity', 'infinity');

SELECT insert_test_result_with_range('SGPT','M', '-infinity','infinity', 0, 42, '-infinity', 'infinity');
SELECT insert_test_result_with_range('SGPT','F', '-infinity','infinity', 0, 32, '-infinity', 'infinity');



-- Numeric
SELECT insert_test_result_with_range('CT (Clotting Time)',4,9);
SELECT insert_test_result_with_range('BT (Bleeding Time)',1,5);
SELECT insert_test_result_with_range('Whole Blood Clotting Time',0,20);
SELECT insert_test_result_with_range('Whole Blood Clotting Time',0,20);
SELECT insert_test_result_with_range('Blood Sugar (Fasting)',70,125);
SELECT insert_test_result_with_range('Blood Sugar (Post Meal)',100,200);
SELECT insert_test_result_with_range('Blood Sugar (Random)',70,200);
SELECT insert_test_result_with_range('Blood Urea ',13,45);
SELECT insert_test_result_with_range('Sr. Albumin', 3.2, 5.0);
SELECT insert_test_result_with_range('Total Protein ', 6.0, 8.3);
SELECT insert_test_result_with_range('Sr. Globulin', 2.3, 3.6);
SELECT insert_test_result_with_range('Albumin / Globulin ratio', 1.0, 2.3);
SELECT insert_test_result_with_range('HbA1C', 0, 7);
SELECT insert_test_result_with_range('TLC (Body Fluid)', '-infinity', 'infinity');
SELECT insert_test_result_with_range('DLC (Body Fluid)', '-infinity', 'infinity');
SELECT insert_test_result_with_range('Protein (Body Fluid)', '-infinity', 'infinity');
SELECT insert_test_result_with_range('Sugar (Body Fluid)', '-infinity', 'infinity');












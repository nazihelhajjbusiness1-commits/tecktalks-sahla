ALTER TABLE delivery_grades DROP CONSTRAINT chk_delivery_grade_value;

UPDATE delivery_grades SET grade = 'GRADE_A' WHERE grade = 'A';
UPDATE delivery_grades SET grade = 'GRADE_B' WHERE grade = 'B';
UPDATE delivery_grades SET grade = 'GRADE_C' WHERE grade = 'C';

ALTER TABLE delivery_grades ADD CONSTRAINT chk_delivery_grade_value
    CHECK (grade IN ('GRADE_A', 'GRADE_B', 'GRADE_C', 'REJECT', 'PREMIUM'));

-- =====================================================
-- V3: Add GAD-7 (Generalized Anxiety Disorder) Assessment
-- =====================================================

-- GAD-7 Assessment Definition
INSERT INTO assessment_definitions (code, name, description, instructions, max_score, active, created_at) VALUES
('GAD7', 'Generalized Anxiety Disorder-7', 
 'A validated screening tool for anxiety disorders. The GAD-7 helps identify symptoms of generalized anxiety disorder.',
 'Over the last 2 weeks, how often have you been bothered by the following problems? Select the response that best describes your experience.',
 21, TRUE, NOW());

-- GAD-7 Questions (7 items)
INSERT INTO assessment_questions (definition_id, question, display_order, option_0_text, option_1_text, option_2_text, option_3_text) VALUES
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Feeling nervous, anxious, or on edge', 1, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Not being able to stop or control worrying', 2, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Worrying too much about different things', 3, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Trouble relaxing', 4, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Being so restless that it''s hard to sit still', 5, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Becoming easily annoyed or irritable', 6, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
((SELECT id FROM assessment_definitions WHERE code = 'GAD7'), 'Feeling afraid as if something awful might happen', 7, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day');

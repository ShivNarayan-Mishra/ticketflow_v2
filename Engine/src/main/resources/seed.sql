-- 1. Clear out any junk data in case you are re-running this script
DELETE FROM claims;
DELETE FROM resources;

-- 2. Seed a RoomResource
-- resource_type is 'ROOM'. Condition and category are left NULL. Location is populated.
-- total_slots is 2, matching the exact requirement for your Week 1 & 2 stress tests.
INSERT INTO resources (id, resource_type, name, total_slots, available_slots, condition, category, location)
VALUES
('ROOM-101', 'ROOM', 'Main Conference Room', 2, 2, NULL, NULL, 'Building A, Floor 1');

-- 3. Seed a GearResource
-- resource_type is 'GEAR'. Location is NULL.
-- Condition ('NEW') and Category ('CAMERA') perfectly match the Java Enums.
INSERT INTO resources (id, resource_type, name, total_slots, available_slots, condition, category, location)
VALUES
('GEAR-001', 'GEAR', 'Sony A7III Camera', 1, 1, 'NEW', 'CAMERA', NULL),
('GEAR-002', 'GEAR', 'Rode NTG4 Shotgun Mic', 5, 5, 'GOOD', 'AUDIO', NULL);
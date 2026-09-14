-- One-off backfill for rows created before cargoType / vehicleType existed.
-- Sets null types to GENERAL so assignment and deadhead-start checks do not 400,
-- and flags those rows so office can review and correct the real type.
--
-- Database: logistics (MySQL)
-- Safe to re-run: only rows that are still NULL are updated; already-flagged
-- rows keep their flag until office PATCHes a type (which clears the flag).

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS vehicle_type VARCHAR(32) NULL;

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS vehicle_type_defaulted TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE logistics_load
    ADD COLUMN IF NOT EXISTS cargo_type VARCHAR(32) NULL;

ALTER TABLE logistics_load
    ADD COLUMN IF NOT EXISTS cargo_type_defaulted TINYINT(1) NOT NULL DEFAULT 0;

UPDATE vehicles
SET vehicle_type = 'GENERAL',
    vehicle_type_defaulted = 1
WHERE vehicle_type IS NULL;

UPDATE logistics_load
SET cargo_type = 'GENERAL',
    cargo_type_defaulted = 1
WHERE cargo_type IS NULL;

-- Review lists for office. Also exposed as vehicleTypeDefaulted / cargoTypeDefaulted
-- on GET /api/vehicles and GET /api/loads (and load DTOs).

SELECT id,
       license_plate,
       make,
       model,
       vehicle_type,
       vehicle_type_defaulted
FROM vehicles
WHERE vehicle_type_defaulted = 1
ORDER BY id;

SELECT id,
       description,
       pickup_location,
       delivery_location,
       status,
       cargo_type,
       cargo_type_defaulted
FROM logistics_load
WHERE cargo_type_defaulted = 1
ORDER BY id;

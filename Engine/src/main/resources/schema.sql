SET search_path TO public;
-- 1. Drop existing tables (Explicitly looking in the public folder)
DROP TABLE IF EXISTS public.claims;
DROP TABLE IF EXISTS public.resources;

-- 2. Create the resources table (Explicitly placing it in the public folder)
CREATE TABLE public.resources (
    id VARCHAR(50) PRIMARY KEY,
    resource_type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    total_slots INT NOT NULL,
    available_slots INT NOT NULL,

    condition VARCHAR(20),
    category VARCHAR(20),
    location VARCHAR(100),

    CONSTRAINT check_slots CHECK (available_slots >= 0 AND available_slots <= total_slots)
);

-- 3. Create the claims table (Explicitly placing it in the public folder)
CREATE TABLE public.claims (
    id VARCHAR(36) PRIMARY KEY,
    resource_id VARCHAR(50) NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    booked_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_resource FOREIGN KEY (resource_id) REFERENCES public.resources(id) ON DELETE CASCADE
);
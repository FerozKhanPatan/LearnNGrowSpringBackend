-- Migration to fix enrollment status check constraint
-- This updates the constraint to accept uppercase enum values matching the Java backend

-- Step 1: Check current constraint definition (for reference)
-- SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'enrollments_status_check';

-- Step 2: Drop the old constraint
ALTER TABLE enrollments
DROP CONSTRAINT IF EXISTS enrollments_status_check;

-- Step 3: Migrate any existing lowercase records to uppercase
UPDATE enrollments
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- Step 4: Create new constraint with uppercase enum values
ALTER TABLE enrollments
ADD CONSTRAINT enrollments_status_check
CHECK (
    status IN (
        'PENDING',
        'APPROVED',
        'REJECTED',
        'CONTACTED',
        'IN_PROGRESS',
        'ACTIVE',
        'COMPLETED',
        'SUSPENDED',
        'CANCELLED'
    )
);

-- Step 5: Verify the constraint was created correctly
-- SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'enrollments_status_check';

-- Step 6: Verify all status values are now uppercase
-- SELECT DISTINCT status FROM enrollments;

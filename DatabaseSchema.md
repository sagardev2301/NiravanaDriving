## Table `instructors`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `auth_user_id` | `uuid` |  Unique |
| `full_name` | `text` |  |
| `phone` | `text` |  Nullable |
| `license_number` | `text` |  Nullable |
| `profile_photo_url` | `text` |  Nullable |
| `created_at` | `timestamptz` |  |
| `updated_at` | `timestamptz` |  |
| `title` | `text` |  Nullable |
| `years_experience` | `int4` |  Nullable |
| `grade` | `text` |  Nullable |
| `email` | `text` |  Nullable |
| `is_active` | `bool` |  |

## Table `students`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `instructor_id` | `uuid` |  |
| `full_name` | `text` |  |
| `phone` | `text` |  Nullable |
| `email` | `text` |  Nullable |
| `date_of_birth` | `date` |  Nullable |
| `license_type` | `license_type` |  Nullable |
| `profile_photo_url` | `text` |  Nullable |
| `created_at` | `timestamptz` |  |
| `updated_at` | `timestamptz` |  |
| `address` | `varchar` |  Nullable |
| `license_number` | `text` |  Nullable |
| `license_issue_date` | `date` |  Nullable |
| `license_expiry_date` | `date` |  Nullable |
| `transmission_type` | `transmission_type` |  Nullable |
| `course_type` | `text` |  Nullable |
| `total_sessions` | `int4` |  Nullable |
| `fee_per_session` | `numeric` |  Nullable |
| `preferred_payment_method` | `payment_method` |  Nullable |
| `instructor_remarks` | `text` |  Nullable |
| `is_active` | `bool` |  |

## Table `skill_categories`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `name` | `text` |  Unique |
| `display_order` | `int4` |  |

## Table `skills`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `category_id` | `uuid` |  |
| `name` | `text` |  |
| `description` | `text` |  Nullable |
| `display_order` | `int4` |  |

## Table `lessons`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `instructor_id` | `uuid` |  |
| `student_id` | `uuid` |  |
| `scheduled_date` | `date` |  |
| `scheduled_time` | `time` |  |
| `duration_minutes` | `int4` |  |
| `status` | `lesson_status` |  |
| `pickup_location` | `text` |  Nullable |
| `notes` | `text` |  Nullable |
| `created_at` | `timestamptz` |  |
| `updated_at` | `timestamptz` |  |
| `vehicle_id` | `uuid` |  Nullable |

## Table `lesson_sessions`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `lesson_id` | `uuid` |  Unique |
| `started_at` | `timestamptz` |  |
| `ended_at` | `timestamptz` |  Nullable |
| `actual_duration_minutes` | `int4` |  Nullable |
| `instructor_notes` | `text` |  Nullable |
| `overall_rating` | `int4` |  Nullable |
| `created_at` | `timestamptz` |  |

## Table `student_skill_progress`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `lesson_session_id` | `uuid` |  |
| `student_id` | `uuid` |  |
| `skill_id` | `uuid` |  |
| `rating` | `int4` |  |
| `notes` | `text` |  Nullable |
| `assessed_at` | `timestamptz` |  |

## Table `payments`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `lesson_id` | `uuid` |  Unique |
| `student_id` | `uuid` |  |
| `instructor_id` | `uuid` |  |
| `amount` | `numeric` |  |
| `currency` | `text` |  |
| `payment_method` | `payment_method` |  |
| `status` | `payment_status` |  |
| `paid_at` | `timestamptz` |  Nullable |
| `notes` | `text` |  Nullable |
| `created_at` | `timestamptz` |  |
| `updated_at` | `timestamptz` |  |

## Table `vehicles`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `instructor_id` | `uuid` |  |
| `make_model` | `text` |  |
| `registration_number` | `text` |  |
| `transmission_type` | `transmission_type` |  |
| `is_active` | `bool` |  |
| `created_at` | `timestamptz` |  |
| `updated_at` | `timestamptz` |  |

## Custom Types / Enums

### `lesson_status`

`scheduled` | `in_progress` | `completed` | `cancelled`

### `payment_status`

`pending` | `paid` | `refunded`

### `payment_method`

`cash` | `upi` | `card` | `bank_transfer`

### `license_type`

`learner` | `provisional` | `full`

### `transmission_type`

`manual` | `automatic` | `both`

## RLS Policies

### `vehicles`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `vehicles_delete_own` | DELETE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `vehicles_insert_own` | INSERT | public | PERMISSIVE | — | `(instructor_id = current_instructor_id())` |
| `vehicles_select_own` | SELECT | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `vehicles_update_own` | UPDATE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |

### `instructors`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `instructors_insert_own` | INSERT | public | PERMISSIVE | — | `(auth_user_id = ( SELECT auth.uid() AS uid))` |
| `instructors_select_own` | SELECT | public | PERMISSIVE | `(auth_user_id = ( SELECT auth.uid() AS uid))` | — |
| `instructors_update_own` | UPDATE | public | PERMISSIVE | `(auth_user_id = ( SELECT auth.uid() AS uid))` | — |

### `skill_categories`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `skill_categories_select` | SELECT | authenticated | PERMISSIVE | `true` | — |

### `lesson_sessions`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `sessions_insert_own` | INSERT | public | PERMISSIVE | — | `(lesson_id IN ( SELECT lessons.id    FROM lessons   WHERE (lessons.instructor_id = current_instructor_id())))` |
| `sessions_select_own` | SELECT | public | PERMISSIVE | `(lesson_id IN ( SELECT lessons.id    FROM lessons   WHERE (lessons.instructor_id = current_instructor_id())))` | — |
| `sessions_update_own` | UPDATE | public | PERMISSIVE | `(lesson_id IN ( SELECT lessons.id    FROM lessons   WHERE (lessons.instructor_id = current_instructor_id())))` | — |

### `student_skill_progress`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `progress_insert_own` | INSERT | public | PERMISSIVE | — | `(student_id IN ( SELECT students.id    FROM students   WHERE (students.instructor_id = current_instructor_id())))` |
| `progress_select_own` | SELECT | public | PERMISSIVE | `(student_id IN ( SELECT students.id    FROM students   WHERE (students.instructor_id = current_instructor_id())))` | — |
| `progress_update_own` | UPDATE | public | PERMISSIVE | `(student_id IN ( SELECT students.id    FROM students   WHERE (students.instructor_id = current_instructor_id())))` | — |

### `skills`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `skills_select` | SELECT | authenticated | PERMISSIVE | `true` | — |

### `lessons`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `lessons_delete_own` | DELETE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `lessons_insert_own` | INSERT | public | PERMISSIVE | — | `(instructor_id = current_instructor_id())` |
| `lessons_select_own` | SELECT | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `lessons_update_own` | UPDATE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |

### `payments`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `payments_insert_own` | INSERT | public | PERMISSIVE | — | `(instructor_id = current_instructor_id())` |
| `payments_select_own` | SELECT | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `payments_update_own` | UPDATE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |

### `students`

| Policy | Command | Roles | Action | USING | WITH CHECK |
|--------|---------|-------|--------|-------|------------|
| `students_delete_own` | DELETE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `students_insert_own` | INSERT | public | PERMISSIVE | — | `(instructor_id = current_instructor_id())` |
| `students_select_own` | SELECT | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |
| `students_update_own` | UPDATE | public | PERMISSIVE | `(instructor_id = current_instructor_id())` | — |


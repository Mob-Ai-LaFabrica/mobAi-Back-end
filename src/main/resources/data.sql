-- Initial Test Data for MobAI Warehouse Backend (MySQL compatible)

-- Insert test users (passwords are BCrypt encoded) - uses INSERT IGNORE to avoid duplicates on restart
-- Password: 'admin123' -> $2a$10$VDKoHpB4kCuf5S1dwbsFC.Ulj1wpBBKc1Eap1qH5Hp6EGKWPvAqJy
-- Password: 'supervisor123' -> $2a$10$v89LD.vJ5kuEHp5/8mEjmOt99EvrCk5Z7P6Uf6.nKJKhCF2.WEQj6
-- Password: 'employee123' -> $2a$10$UXm2U4oE0YCJzKhNvLKPe.xPeREFfCjT.n.KmZUeODCREJKEyu9E6

insert into users (
   id_utilisateur,
   username,
   email,
   password,
   first_name,
   last_name,
   nom_complet,
   role,
   actif,
   account_non_expired,
   account_non_locked,
   credentials_non_expired,
   created_at,
   updated_at
) values ( unhex(replace(
   '123e4567-e89b-12d3-a456-426614174000',
   '-',
   ''
)),
           'admin',
           'admin',
           '$2a$10$VDKoHpB4kCuf5S1dwbsFC.Ulj1wpBBKc1Eap1qH5Hp6EGKWPvAqJy',
           'System',
           'Administrator',
           'System Administrator',
           'ADMIN',
           true,
           true,
           true,
           true,
           now(),
           now() ),( unhex(replace(
   '223e4567-e89b-12d3-a456-426614174001',
   '-',
   ''
)),
                     'supervisor1',
                     'supervisor',
                     '$2a$10$v89LD.vJ5kuEHp5/8mEjmOt99EvrCk5Z7P6Uf6.nKJKhCF2.WEQj6',
                     'John',
                     'Supervisor',
                     'John Supervisor',
                     'SUPERVISOR',
                     true,
                     true,
                     true,
                     true,
                     now(),
                     now() ),( unhex(replace(
   '323e4567-e89b-12d3-a456-426614174002',
   '-',
   ''
)),
                               'employee1',
                               'employee',
                               '$2a$10$UXm2U4oE0YCJzKhNvLKPe.xPeREFfCjT.n.KmZUeODCREJKEyu9E6',
                               'Jane',
                               'Employee',
                               'Jane Employee',
                               'EMPLOYEE',
                               true,
                               true,
                               true,
                               true,
                               now(),
                               now() );
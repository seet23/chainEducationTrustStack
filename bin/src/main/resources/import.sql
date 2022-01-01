insert into users (first_name, surname, login, password, email, phone, language, enabled) values ('Steve', 'JOBS', 'steve', '$2a$10$hewJVE.r8GFd4sz3cVPfB.OUC/3ya0R6HMQe1D3iwF3.VgX4Va/5e', 'steve.jobs@apple.com', '0033 1 23 45 67 89', 'en', true);
insert into users (first_name, surname, login, password, email, phone, language, enabled) values ('Bill', 'GATES', 'bill', '$2a$10$8IbzjsdV16bS1hbMWgspAO1KcxxWAc1VCmQ8LJERP48Fp5lzbVzdG', 'bill.gates@microsoft.com', '0033 1 23 45 67 89', 'fr', true);
insert into users (first_name, surname, login, password, email, phone, language, enabled) values ('Chinedu', 'OKAFOR', 'chinedu', '$2a$10$2lHvYp5/bvxTlmCpBdpDWu9zdeyKRuxCi8co.qVGiUf/tsMvRIyXC', 'csokafor@github.com', '0033 1 23 45 67 89', 'en', true);

insert into authority (name) values ('admin');
insert into authority (name) values ('technical user');
insert into authority (name) values ('user');

insert into users_authority (id_user, id_authority) values (1, 1);
insert into users_authority (id_user, id_authority) values (1, 2);
insert into users_authority (id_user, id_authority) values (1, 3);
insert into users_authority (id_user, id_authority) values (2, 3);
insert into users_authority (id_user, id_authority) values (3, 3);
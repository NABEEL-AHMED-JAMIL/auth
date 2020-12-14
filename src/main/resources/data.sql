-- Default Authority Script ---
insert into authority values(1000, current_date, 0, 1, current_date,0,'ROLE_SUPER_ADIM');
insert into authority values (1001, current_date, 0, 1, current_date,0,'ROLE_ADMIN');
insert into authority values (1002, current_date, 0, 1, current_date,0,'ROLE_USER');
-- Default Access-Service Script ---
insert into access_service values (1000, current_date, 0, 1, current_date, 0, 'Service-1', 'WebScraping');
insert into access_service values (1001, current_date, 0, 1, current_date, 0, 'Service-2', 'DataFetch');
insert into access_service values (1002, current_date, 0, 1, current_date, 0, 'Service-3', 'FireStream');
-- Default User Script ---
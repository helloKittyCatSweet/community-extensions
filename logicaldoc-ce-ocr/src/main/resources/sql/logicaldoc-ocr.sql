create table ld_ocr_history (ld_id bigint not null, ld_lastmodified timestamp not null, ld_creation timestamp not null, ld_recordversion bigint not null, 
                             ld_deleted int not null, ld_tenantid bigint not null, ld_date timestamp, ld_event varchar(255), 
                             ld_comment varchar(4000), ld_docid bigint, ld_folderid bigint, ld_new int, ld_filename varchar(255), 
                             ld_filesize bigint, ld_path varchar(4000), ld_color varchar(255), ld_userid bigint, 
                             ld_username varchar(255), ld_userlogin varchar(255), primary key (ld_id));

insert into hibernate_sequences(sequence_name, next_val) values ('ld_ocr_history', 100);

insert into ld_menu 
           (ld_id,ld_lastmodified,ld_creation,ld_deleted,ld_name,ld_parentid,ld_icon,ld_type,ld_tenantid,ld_recordversion,ld_position,ld_enabled)
values     (104,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'ocr',7,'ocr.png',1,1,1,6,10);

insert into ld_user
           (ld_id,ld_lastmodified,ld_creation,ld_deleted,ld_enabled,ld_username,ld_password,ld_name,ld_firstname,ld_street,ld_postalcode,ld_city,ld_country,ld_language,ld_email,ld_telephone,ld_type,ld_passwordchanged,ld_passwordexpires,ld_source,ld_quota,ld_passwordexpired,ld_tenantid,ld_recordversion,ld_enforcewrktime)
values     (-1080,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,1,'_ocr','','OCR','','','','','','en','ocr@acme.com','',1,null,0,0,-1,0,1,1,0);
insert into ld_group(ld_id,ld_lastmodified,ld_creation,ld_deleted,ld_name,ld_description,ld_type,ld_tenantid,ld_recordversion)
values     (-1080,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,'_user_-1080','',1,1,1);
insert into ld_usergroup
values (-1080,-1080);
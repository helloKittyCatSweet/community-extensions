insert into ld_tenant(ld_id,ld_lastmodified,ld_deleted,ld_tenantid,ld_name,ld_displayname,ld_type,ld_enabled,ld_recordversion)
values     (2,CURRENT_TIMESTAMP,0,2,'tenant2','Tenant 2',0,1,1);

insert into ld_user
           (ld_id,ld_lastmodified,ld_deleted,ld_enabled,ld_username,ld_password,ld_name,ld_firstname,ld_street,ld_postalcode,ld_city,ld_country,ld_language,ld_email,ld_telephone,ld_type,ld_passwordchanged,ld_passwordexpires,ld_source,ld_quota,ld_passwordexpired,ld_tenantid,ld_recordversion,ld_enforcewrktime)
values     (2,'2008-10-22 00:00:00',0,1,'boss','d033e22ae348aeb566fc214aec3585c4da997','Meschieri','Marco','','','','','it','m.meschieri@logicalobjects.it','',0,null,0,0,-1,0,1,1,0);
insert into ld_group
           (ld_id,ld_lastmodified,ld_deleted,ld_tenantid,ld_name,ld_type,ld_recordversion)
values     (-2,'2008-10-22 00:00:00',0,1,'_user_2',1,1);
insert into ld_usergroup
values (-2,2);

insert into ld_user
           (ld_id,ld_lastmodified,ld_deleted,ld_enabled,ld_username,ld_password,ld_name,ld_firstname,ld_street,ld_postalcode,ld_city,ld_country,ld_language,ld_email,ld_telephone,ld_type,ld_passwordchanged,ld_passwordexpires,ld_source,ld_quota,ld_passwordexpired,ld_tenantid,ld_recordversion,ld_enforcewrktime)
values     (3,'2008-10-22 00:00:00',0,1,'sebastian','d033e22ae348aeb566fc214aec3585c4da997','Sebastian','Stein','','','','','de','seb_stein@gmx.de','',0,null,0,0,-1,0,1,1,0);
insert into ld_group
           (ld_id,ld_lastmodified,ld_deleted,ld_tenantid,ld_name,ld_type,ld_recordversion)
values     (-3,'2008-10-22 00:00:00',0,1,'_user_3',1,1);
insert into ld_usergroup
values (-3,3);

insert into ld_user
           (ld_id,ld_lastmodified,ld_deleted,ld_enabled,ld_username,ld_password,ld_name,ld_firstname,ld_street,ld_postalcode,ld_city,ld_country,ld_language,ld_email,ld_telephone,ld_type,ld_passwordchanged,ld_passwordexpires,ld_source,ld_quota,ld_passwordexpired,ld_tenantid,ld_recordversion,ld_enforcewrktime)
values     (4,'2008-10-22 00:00:00',0,1,'author','d033e22ae348aeb566fc214aec3585c4da997','Author','Author','','','','','de','author@acme.com','',0,null,0,0,-1,0,1,1,0);
insert into ld_group
           (ld_id,ld_lastmodified,ld_deleted,ld_tenantid,ld_name,ld_type,ld_recordversion)
values     (-4,'2008-10-22 00:00:00',0,1,'_user_4',1,1);
insert into ld_usergroup
values (-4,4);

insert into ld_user
           (ld_id,ld_lastmodified,ld_deleted,ld_enabled,ld_username,ld_password,ld_name,ld_firstname,ld_street,ld_postalcode,ld_city,ld_country,ld_language,ld_email,ld_telephone,ld_type,ld_passwordchanged,ld_passwordexpires,ld_source,ld_quota,ld_passwordexpired,ld_tenantid,ld_recordversion,ld_enforcewrktime)
values     (5,'2008-10-22 00:00:00',0,1,'test','d033e22ae348aeb566fc214aec3585c4da997','test','Test','','','','','de','test@acme.com','',0,null,0,0,-1,0,1,1,0);
insert into ld_group
           (ld_id,ld_lastmodified,ld_deleted,ld_tenantid,ld_name,ld_type,ld_recordversion)
values     (-5,'2008-10-22 00:00:00',0,1,'_user_5',1,1);
insert into ld_usergroup
values (-5,5);

insert into ld_menu
           (ld_id,ld_lastmodified,ld_deleted,ld_name,ld_parentid,ld_icon,ld_type,ld_tenantid,ld_recordversion,ld_position,ld_enabled)
values     (99,'2008-10-22 00:00:00',0,'menu.admin',2,'administration.gif',5,1,1,1,1);

insert into ld_menu
           (ld_id,ld_lastmodified,ld_deleted,ld_name,ld_parentid,ld_icon,ld_type,ld_tenantid,ld_recordversion,ld_position,ld_enabled)
values     (2000,'2008-10-22 00:00:00',0,'menu.admin',2,'administration.gif',3,1,1,1,1);

insert into ld_menu
           (ld_id,ld_lastmodified,ld_deleted,ld_name,ld_parentid,ld_icon,ld_type,ld_tenantid,ld_recordversion,ld_position,ld_enabled)
values     (101,'2008-10-22 00:00:00',0,'text',2000,'administration.gif',3,1,1,1,1);

insert into ld_menu
           (ld_id,ld_lastmodified,ld_deleted,ld_name,ld_parentid,ld_icon,ld_type,ld_tenantid,ld_recordversion,ld_position,ld_enabled)
values     (102,'2008-10-22 00:00:00',0,'menu.admin',101,'administration.gif',5,1,1,1,1);
insert into ld_menu
           (ld_id,ld_lastmodified,ld_deleted,ld_name,ld_parentid,ld_icon,ld_type,ld_tenantid,ld_recordversion,ld_position,ld_enabled)
values     (103,'2008-10-22 00:00:00',0,'menu.admin',101,'administration.gif',3,1,1,1,1);

insert into ld_menu_acl
values     (2000,1,1,1);

insert into ld_menu_acl
values     (2000,3,1,1);

insert into ld_menu_acl
values     (103,1,1,1);

insert into ld_menu_acl
values     (103,2,1,1);

insert into ld_menu_acl
values     (99,1,1,0);

insert into ld_usergroup
           (ld_userid,ld_groupid)
values     (3,1);

insert into ld_usergroup
           (ld_userid,ld_groupid)
values     (3,2);

insert into ld_usergroup
           (ld_userid,ld_groupid)
values     (4,2);

insert into ld_usergroup
           (ld_userid,ld_groupid)
values     (5,3);

insert into ld_group
           (ld_id,ld_lastmodified,ld_deleted,ld_tenantid,ld_name,ld_type,ld_recordversion)
values     (10,'2008-10-22 00:00:00',0,1,'testGroup',0,1);

insert into ld_document
           (ld_id,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_links,ld_docattrs)
values     (1,5,'2008-10-22 00:00:00',0,'testDocV01','2006-12-19 00:00:00','myself',1,1,'PDF',3,'en','pippo.pdf',1356,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);
insert into ld_version
           (ld_id,ld_documentid,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_links,ld_docattrs)
values     (998,1,5,'2008-10-22 00:00:00',0,'testDocV01','2006-12-19 00:00:00','myself',1,1,'PDF',3,'en','pippo.pdf',1356,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);

insert into ld_document
           (ld_id,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_links,ld_docattrs)
values     (2,5,'2008-10-22 00:00:00',0,'testDocV02','2006-12-19 00:00:00','myself',1,1,'PPT',3,'en','pluto.txt',1223,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);
insert into ld_version
           (ld_id,ld_documentid,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_link,ld_docattrss)
values     (999,2,5,'2008-10-22 00:00:00',0,'testDocV02','2006-12-19 00:00:00','myself',1,1,'PPT',3,'en','pluto.txt',1223,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);

insert into ld_document
           (ld_id,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_links,ld_docattrs)
values     (3,5,'2008-10-22 00:00:00',1,'testDocV03','2006-12-19 00:00:00','myself',1,1,'PDF',3,'en','pluto.xsl',122345,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);

insert into ld_document
           (ld_id,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_links,ld_docattrs)
values     (4,5,'2008-10-22 00:00:00',0,'testDocV04','2006-12-19 00:00:00','myself',1,1,'TXT',3,'en','pluto.zas',122345,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);

insert into ld_version
           (ld_id,ld_documentid,ld_folderid,ld_lastmodified,ld_deleted,ld_version,ld_date,ld_publisher,ld_publisherid,ld_status,ld_type,ld_lockuserid,ld_language,ld_filename,ld_filesize,ld_indexed, ld_creation, ld_immutable,ld_signed,ld_creator,ld_creatorid,ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages,ld_stamped,ld_nature,ld_links,ld_docattrs)
values     (997,4,5,'2008-10-22 00:00:00',0,'testDocV04','2006-12-19 00:00:00','myself',1,1,'TXT',3,'en','pluto.zas',122345,1,'2008-12-03 00:00:00',0,0,'',1,0,0,1,1,1,5,0,0,0,0);

insert into ld_ticket
           (ld_id,ld_lastmodified,ld_deleted,ld_ticketid,ld_docid,ld_userid,ld_type,ld_creation,ld_expired,ld_count,ld_tenantid,ld_recordversion,ld_views)
values     (1,'2008-10-22 00:00:00',0,'1',1,1,0,'2011-01-01 00:00:00','2011-01-02 00:00:00',0,1,1,0);

insert into ld_ticket
           (ld_id,ld_lastmodified,ld_deleted,ld_ticketid,ld_docid,ld_userid,ld_type,ld_creation,ld_expired,ld_count,ld_tenantid,ld_recordversion,ld_views)
values     (2,'2008-10-22 00:00:00',0,'2',2,3,0,'2011-01-01 00:00:00','2011-01-02 00:00:00',0,1,1,0);

insert into ld_ticket
           (ld_id,ld_lastmodified,ld_deleted,ld_ticketid,ld_docid,ld_userid,ld_type,ld_creation,ld_expired,ld_count,ld_tenantid,ld_recordversion,ld_views)
values     (3,'2008-10-22 00:00:00',0,'3',1,3,0,'2011-01-01 00:00:00','2011-01-02 00:00:00',0,1,1,0);

insert into ld_version(ld_id, ld_documentid, ld_version, ld_fileversion, ld_username, ld_userid, ld_versiondate, ld_comment, ld_lastmodified, ld_deleted, ld_immutable, ld_creation, ld_publisherid, ld_indexed, ld_signed, ld_status, ld_filesize, ld_folderid, ld_filename, ld_customid, ld_creator, ld_creatorid, ld_date, ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages, ld_stamped, ld_nature,ld_links)
values     (1,1,'testVer01','fileVer01','testUser1',1,'2006-12-19 00:00:00','testComment','2009-02-09 00:00:00',0,0,'2009-02-09 00:00:00',1,0,0,0,0,5,'pippo.pdf', '2343453','',1,'2006-12-19 00:00:00',0,0,1,1,1,5,0,0,0,0);

insert into ld_version(ld_id, ld_documentid, ld_version, ld_fileversion, ld_username, ld_userid, ld_versiondate, ld_comment, ld_lastmodified, ld_deleted, ld_immutable, ld_creation, ld_publisherid, ld_indexed, ld_signed, ld_status, ld_filesize, ld_folderid, ld_filename,ld_creator,ld_creatorid, ld_date, ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages, ld_stamped, ld_nature,ld_links)
values     (2,2,'testVer02','fileVer02','testUser2',1,'2006-12-20 00:00:00','testComment','2009-02-09 00:00:00',0,0,'2009-02-09 00:00:00',1,0,0,0,0,5,'paperino.pdf','',1,'2006-12-20 00:00:00',0,0,1,1,1,5,0,0,0,0);

insert into ld_version(ld_id, ld_documentid, ld_version, ld_fileversion, ld_username, ld_userid, ld_versiondate, ld_comment, ld_lastmodified, ld_deleted, ld_immutable, ld_creation, ld_publisherid, ld_indexed, ld_signed, ld_status, ld_filesize, ld_folderid, ld_filename, ld_customid,ld_creator,ld_creatorid, ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages, ld_stamped, ld_nature,ld_links)
values     (3,3,'testVer03','fileVer03','testUser',1,'2009-02-11 00:00:00','testArchive','2009-02-09 00:00:00',0,0,'2009-02-09 00:00:00',1,0,0,0,0,5,'pluto.txt', '4532fgr','',1,0,0,1,1,1,5,0,0,0,0);

insert into ld_version(ld_id, ld_documentid, ld_version, ld_fileversion, ld_username, ld_userid, ld_versiondate, ld_comment, ld_lastmodified, ld_deleted, ld_immutable, ld_creation, ld_publisherid, ld_indexed, ld_signed, ld_status, ld_filesize, ld_folderid, ld_filename,ld_creator,ld_creatorid, ld_exportstatus, ld_barcoded,ld_published,ld_tenantid,ld_recordversion,ld_pages, ld_stamped, ld_nature,ld_links)
values     (4,4,'testVer04','fileVer04','testUser',1,'2009-02-11 00:00:00','testArchive','2009-02-09 00:00:00',1,0,'2009-02-09 00:00:00',1,0,0,0,0,5,'topolino.pdf','',1,0,0,1,1,1,5,0,0,0,0);

insert into  ld_version_ext (ld_versionid, ld_mandatory, ld_type, ld_position, ld_stringvalue, ld_name, ld_editor)
values (1, 0, 0, 0, 'pippo', 'paperino', 0);

insert into  ld_version_ext (ld_versionid, ld_mandatory, ld_type, ld_position, ld_stringvalue, ld_name, ld_editor)
values (1, 0, 0, 0, 'pluto', 'topolino', 0);


insert into ld_history 
				(ld_id, ld_lastmodified, ld_deleted, ld_docid, ld_folderid, ld_userid, ld_date, ld_username, ld_event, ld_comment, ld_version, ld_notified,ld_tenantid,ld_recordversion)
values     (1,'2008-10-22 00:00:00',0,1,5,1,'2006-12-20 00:00:00','author','data test 01','','',0,1,1);

insert into ld_history 
				(ld_id, ld_lastmodified, ld_deleted, ld_docid, ld_folderid, ld_userid, ld_date, ld_username, ld_event, ld_comment, ld_version, ld_notified,ld_tenantid,ld_recordversion)
values     (2,'2008-10-22 00:00:00',0,2,5,1,'2006-12-25 00:00:00','author','data test 02','','',0,1,1);

insert into ld_history 
				(ld_id, ld_lastmodified, ld_deleted, ld_docid, ld_folderid, ld_userid, ld_date, ld_username, ld_event, ld_comment, ld_version, ld_notified,ld_tenantid,ld_recordversion)
values     (3,'2008-10-22 00:00:00',0,1,5,3,'2006-12-27 00:00:00','sebastian','data test 03','','',1,1,1);

insert into ld_link(ld_id, ld_lastmodified,ld_deleted, ld_docid1, ld_docid2,ld_type,ld_tenantid,ld_recordversion)
values   (1,'2008-10-22 00:00:00',0,1,2,'test',1,1);
insert into ld_link(ld_id, ld_lastmodified,ld_deleted, ld_docid1, ld_docid2,ld_type,ld_tenantid,ld_recordversion)
values   (2,'2008-10-22 00:00:00',0,2,1,'xyz',1,1);
insert into ld_link(ld_id, ld_lastmodified,ld_deleted, ld_docid1, ld_docid2,ld_type,ld_tenantid,ld_recordversion)
values   (3,'2008-10-22 00:00:00',0,1,2,'xxx',1,1);
insert into ld_link(ld_id, ld_lastmodified,ld_deleted, ld_docid1, ld_docid2,ld_type,ld_tenantid,ld_recordversion)
values   (4,'2008-10-22 00:00:00',0,2,1,'',1,1);

insert into ld_template (ld_id, ld_lastmodified,ld_deleted, ld_name, ld_description, ld_readonly, ld_type,ld_tenantid,ld_recordversion)
values (1,'2008-11-07 00:00:00',0,'test1','test1_desc',0,0,1,1);
INSERT INTO LD_TEMPLATE_EXT (LD_TEMPLATEID,LD_MANDATORY,LD_TYPE,LD_POSITION,LD_STRINGVALUE,LD_INTVALUE,LD_DOUBLEVALUE,LD_DATEVALUE,LD_NAME,LD_EDITOR) VALUES (1,1,0,0,null,null,null,null,'piva',0);

insert into ld_template (ld_id, ld_lastmodified,ld_deleted, ld_name, ld_description, ld_readonly, ld_type,ld_tenantid,ld_recordversion)
values (2,'2008-11-07 00:00:00',0,'test2','test2_desc',0,0,1,1);
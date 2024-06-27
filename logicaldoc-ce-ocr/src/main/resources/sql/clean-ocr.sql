update ld_ocr_history set ld_deleted=2 where not exists (select ld_id from ld_tenant where ld_tenantid=ld_id and ld_deleted=0);
  
delete from ld_ocr_history where ld_docid in (select ld_id from ld_document where ld_deleted > 0);

delete from ld_ocr_history where ld_deleted > 0;
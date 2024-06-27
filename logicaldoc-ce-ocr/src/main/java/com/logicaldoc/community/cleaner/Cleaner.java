package com.logicaldoc.community.cleaner;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaldoc.core.task.Task;
import com.logicaldoc.core.task.TaskListener;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.io.FileUtil;
import com.logicaldoc.util.io.ResourceUtil;

/**
 * A cleaner is a specialized class that perform a particular cleanup. For
 * example deletes from db all logically deleted items.
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 4.0
 */
public abstract class Cleaner implements TaskListener {

	protected Logger log = null;

	private String dbScript;

	protected Connection connection;

	protected boolean interruptRequested = false;

	protected Cleaner(Connection con) {
		this();
		this.connection = con;
	}

	protected Cleaner() {
		super();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	public void setLogger(Logger logger) {
		this.log = logger;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Gets the fully qualified name of the database script
	 * 
	 * @return name of the database script
	 */
	public String getDbScript() {
		return dbScript;
	}

	public void setDbScript(String dbScript) {
		this.dbScript = dbScript;

		// Check if there is a specific script for the current DBMS
		ContextProperties conf = Context.get().getProperties();
		String test = dbScript + "." + conf.getProperty("jdbc.dbms").toLowerCase();
		if (ResourceUtil.existsResource(test))
			this.dbScript = test;
	}

	/**
	 * This method is invoked before executing the SQL file. Implementations
	 * could use this to expand placeholders.
	 * 
	 * @param file the SQL script file
	 */
	protected void preprocessDbScript(File file) {
		// Nothing to do
	}

	/**
	 * Performs the cleanup
	 * 
	 * @throws SQLException Error in the database
	 * @throws IOException I/O error
	 */
	public void clean() throws SQLException, IOException {
		File file = FileUtil.createTempFile("clean", ".sql");
		try {
			FileUtil.copyResource(getDbScript(), file);
			preprocessDbScript(file);
			SqlFile sFile = new SqlFile(file, "Cp1252", false);

			if (!interruptRequested)
				beforeDbUpdate();

			sFile.setContinueOnError(true);
			sFile.setConnection(connection);

			if (!interruptRequested)
				sFile.execute();

			if (!interruptRequested)
				afterDbUpdate();
		} catch (SqlToolError e) {
			throw new SQLException(e.getMessage(), e);
		} finally {
			FileUtil.strongDelete(file);
		}
	}

	/**
	 * Invoked before database update
	 * 
	 * @throws SQLException Error in the database
	 */
	protected void beforeDbUpdate() throws SQLException {
		// Nothing to do
	}

	/**
	 * Invoked after database update
	 * 
	 * @throws SQLException Error in the database
	 */
	protected void afterDbUpdate() throws SQLException {
		// Nothing to do
	}

	@Override
	public void progressChanged(long progress) {
		// Nothing to do
	}

	@Override
	public void statusChanged(int status) {
		interruptRequested = status != Task.STATUS_RUNNING;
	}
}
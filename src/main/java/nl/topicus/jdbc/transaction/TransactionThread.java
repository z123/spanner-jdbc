package nl.topicus.jdbc.transaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.cloud.spanner.TransactionRunner.TransactionCallable;

class TransactionThread extends Thread
{
	private DatabaseClient dbClient;

	private boolean stop;

	private boolean stopped;

	private boolean commit;

	private List<Mutation> mutations = new ArrayList<>(40);

	private BlockingQueue<Statement> statements = new LinkedBlockingQueue<>();

	private BlockingQueue<ResultSet> resultSets = new LinkedBlockingQueue<>();

	TransactionThread(DatabaseClient dbClient)
	{
		this.dbClient = dbClient;
		setDaemon(true);
	}

	@Override
	public void run()
	{
		TransactionRunner runner = dbClient.readWriteTransaction();
		runner.run(new TransactionCallable<Void>()
		{

			@Override
			public Void run(TransactionContext transaction) throws Exception
			{
				while (!stop)
				{
					try
					{
						Statement statement = statements.take();
						if (!(statement.getSql().equals("commit") || statement.getSql().equals("rollback")))
						{
							resultSets.put(transaction.executeQuery(statement));
						}
					}
					catch (InterruptedException e)
					{
						System.err.println("Transaction interrupted while waiting for statement");
					}
				}

				if (commit)
				{
					transaction.buffer(mutations);
				}
				stopped = true;
				transaction.close();
				return null;
			}
		});
	}

	ResultSet executeQuery(Statement statement)
	{
		try
		{
			statements.put(statement);
			return resultSets.take();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("Query execution interrupted", e);
		}
	}

	void buffer(Mutation mutation)
	{
		mutations.add(mutation);
	}

	void buffer(Iterable<Mutation> mutations)
	{
		Iterator<Mutation> it = mutations.iterator();
		while (it.hasNext())
			buffer(it.next());
	}

	void commit() throws SQLException
	{
		stopTransaction(true);
	}

	void rollback() throws SQLException
	{
		stopTransaction(false);
	}

	private void stopTransaction(boolean commit) throws SQLException
	{
		this.commit = commit;
		stop = true;
		// Add a null object in order to get the transaction thread to proceed
		statements.add(Statement.of(commit ? "commit" : "rollback"));
		while (!stopped)
		{
			try
			{
				Thread.sleep(0, 5);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

}

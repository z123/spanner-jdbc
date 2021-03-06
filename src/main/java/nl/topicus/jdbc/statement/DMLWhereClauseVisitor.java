package nl.topicus.jdbc.statement;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.schema.Column;

abstract class DMLWhereClauseVisitor extends ExpressionVisitorAdapter
{
	private Column col;

	private ParameterStore parameterStore;

	DMLWhereClauseVisitor(ParameterStore parameterStore)
	{
		this.parameterStore = parameterStore;
	}

	protected ParameterStore getParameterStore()
	{
		return parameterStore;
	}

	@Override
	public void visit(Column column)
	{
		// Ideally we should check here whether this column is actually part of
		// the primary key, but that would involve another roundtrip to the
		// database.
		this.col = column;
	}

	protected abstract void visitExpression(Column col, Expression expression);

	@Override
	public void visit(JdbcParameter parameter)
	{
		visitExpression(col, parameter);
	}

	@Override
	public void visit(DoubleValue value)
	{
		visitExpression(col, value);
	}

	@Override
	public void visit(LongValue value)
	{
		visitExpression(col, value);
	}

	@Override
	public void visit(DateValue value)
	{
		visitExpression(col, value);
	}

	@Override
	public void visit(TimeValue value)
	{
		visitExpression(col, value);
	}

	@Override
	public void visit(TimestampValue value)
	{
		visitExpression(col, value);
	}

	@Override
	public void visit(StringValue value)
	{
		visitExpression(col, value);
	}

	@Override
	public void visit(HexValue value)
	{
		visitExpression(col, value);
	}

}

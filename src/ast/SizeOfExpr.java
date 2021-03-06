package ast;

public class SizeOfExpr extends Expr {
	public Type type;

	public SizeOfExpr(Type type) {
		this.type = type;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitSizeOfExpr(this);
	}
}

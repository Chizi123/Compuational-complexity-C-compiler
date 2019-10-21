package sem;

import ast.*;

import java.util.Hashtable;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	private Hashtable<String, StructTypeDecl> structs;

	@Override
	public Type visitBaseType(BaseType bt) {
		if (bt.type == BaseTypeEnum.VOID) {
			error("Declaration of variable of type \"VOID\"");
		}
		// To be completed...
		return bt.type;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		structs.put(st.st.name, st);

		for (VarDecl i : st.varDeclList) {
			i.accept(this);
		}
		// To be completed...
		return null;
	}

	@Override
	public Type visitBlock(Block b) {
		for (VarDecl i : b.varDeclList) {
			i.accept(this);
		}

		for (Stmt i : b.stmtList) {
			i.accept(this);
		}
		// To be completed...
		return null;
	}

	private Type funretT;
	@Override
	public Type visitFunDecl(FunDecl p) {
		funretT = p.type;
		//check for void parameters
		for (VarDecl i : p.params) {
			i.accept(this);
		}
		// To be completed...
		p.block.accept(this);
		return null;
	}

	@Override
	public Type visitProgram(Program p) {
		structs = new Hashtable<>();
		for (StructTypeDecl i : p.structTypeDecls) {
			i.accept(this);
		}

		for (VarDecl i : p.varDecls) {
			i.accept(this);
		}

		for (FunDecl i : p.funDecls) {
			i.accept(this);
		}
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		//check for void variables
		vd.type.accept(this);
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		v.type = v.vd.type;
		// To be completed...
		return v.type;
	}

	// To be completed...


	public TypeCheckVisitor() {
		super();
	}

	@Override
	public int getErrorCount() {
		return super.getErrorCount();
	}

	@Override
	protected void error(String message) {
		super.error(message);
	}

	@Override
	public Type visitPointerType(PointerType pt) {
		return pt;
	}

	@Override
	public Type visitStructType(StructType st) {
		if (!(structs.keySet().contains(st.name))) {
			error("Use of undeclared struct type "+st.name);
		}
		return st;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		return at;
	}

	@Override
	public Type visitIntLiteral(IntLiteral il) {
		return il.type.accept(this);
	}

	@Override
	public Type visitStringLiteral(StrLiteral sl) {
		//unsure about string length
		return sl.type.accept(this);
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		return cl.type.accept(this);
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		fce.type = fce.fd.type;
		return fce.type;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
		Type e1 = bo.E1.accept(this);
		Type e2 = bo.E2.accept(this);
		if (bo.op.op == OpEnum.NE || bo.op.op == OpEnum.EQ) {
			if ((e1 instanceof StructType || e1 instanceof ArrayType || e1.accept(this) == BaseTypeEnum.VOID) &&
					(e2 instanceof StructType || e2 instanceof ArrayType || e2.accept(this) == BaseTypeEnum.VOID) &&
					e1.accept(this) != e2.accept(this)) {
				error("Bad argument types for equality comparison");
			} else {
				bo.type = e1;
				return bo.type;
			}
		} else {
			if (e1.accept(this) == BaseTypeEnum.INT && e2.accept(this) == BaseTypeEnum.INT) {
				bo.type = BaseTypeEnum.INT;
				return bo.type;
			} else {
				error("BinOp with expressions that aren't INT");
				return null;
			}
		}
		return bo.type;
	}

	@Override
	public Type visitOp(Op o) {
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		aae.exp.accept(this);
		if ((aae.exp.type instanceof ArrayType || aae.exp.type instanceof PointerType) && aae.index.type.accept(this) == BaseTypeEnum.INT) {
			aae.type = aae.exp.type.accept(this);
			return aae.type;
		} else {
			error("Array access to instance not array or pointer");
		}
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		Type t = fae.struct.accept(this);
		StructTypeDecl f = structs.get((((StructType) t).name));
		fae.type = null;
		for (VarDecl i: f.varDeclList) {
			if (i.varName.equals(fae.field)) {
				fae.type = i.type;
				break;
			}
		}
		if (fae.type == null) {
			error("Accessing field "+fae.field+" on struct "+f.st.name+"which doesn't exist");
		}
		return fae.type;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		if (vae.exp.accept(this) instanceof PointerType) {
			vae.type = vae.exp.type;
			return vae.type;
		} else {
			error("Pointer reference to instance not pointer");
		}
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		return new BaseType(BaseTypeEnum.INT);
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		if (te.type.accept(this) == BaseTypeEnum.INT && te.exp.type.accept(this) == BaseTypeEnum.CHAR) {
			return new BaseType(BaseTypeEnum.INT);
		} else if (te.exp.type instanceof ArrayType) {
			if ((te.type) == ((ArrayType) te.exp.type).type) {
				return new PointerType(te.type);
			} else {
				error("Casting array to pointer of different type");
			}
		} else if (te.exp.type instanceof PointerType) {
			return new PointerType(te.type);
		} else {
			error("Invalid Type cast");
		}
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
		return null;
	}

	@Override
	public Type visitWhile(While w) {
		if (w.cond.accept(this) != BaseTypeEnum.INT) {
			error("While condition not int");
		}
		return null;
	}

	@Override
	public Type visitIf(If i) {
		if (i.cond.accept(this) != BaseTypeEnum.INT) {
			error("If condition not int");
		}
		return null;
	}

	@Override
	public Type visitAssign(Assign a) {
		Type e1 = a.e1.accept(this);
		Type e2 = a.e2.accept(this);
		if ((e1.accept(this) == BaseTypeEnum.VOID || e1 instanceof ArrayType) &&
				(e2.accept(this) == BaseTypeEnum.VOID || e2 instanceof ArrayType)) {
			error("Assignment of Void or Array Types");
		}
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		if (funretT.accept(this) == BaseTypeEnum.VOID) {
			if (r.exp != null) {
				error("Trying to return value from void function");
			}
		} else {
			if (funretT.accept(this) != r.exp.accept(this)) {
				error("Returning wrong type from function");
			}
		}
		return null;
	}
}

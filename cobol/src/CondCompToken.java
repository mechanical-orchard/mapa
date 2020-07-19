public interface CondCompToken {

	public long getSortKey();

	public int getType();

	enum CondCompTokenType {
			DEFINE_ONLY
			, VAR_INTEGER
			, VAR_BOOLEAN
			, VAR_ALPHANUM
			, NUMOP_ADD
			, NUMOP_SUBTRACT
			, NUMOP_MULTIPLY
			, NUMOP_DIVIDE
			, LOGICOP_AND
			, LOGICOP_OR
			, GROUPOP_BEGIN
			, GROUPOP_END
			, COMPAREOP_EQ
			, COMPAREOP_NE
			, COMPAREOP_LT
			, COMPAREOP_LE
			, COMPAREOP_GT
			, COMPAREOP_GE
			, SIMPLE_RELATION
	}


}

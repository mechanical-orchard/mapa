import java.io.File
import java.util.*
import java.util.logging.Logger
import org.antlr.v4.runtime.tree.*

class PPListener() : JCLPPParserBaseListener() {
    override fun enterJobCard(ctx: JCLPPParser.JobCardContext) {

    }

    override fun enterJcllibStatement(ctx: JCLPPParser.JcllibStatementContext) {

    }

    override fun enterCommandStatement(ctx: JCLPPParser.CommandStatementContext) {
    }

    override fun enterJclCommandStatement(ctx: JCLPPParser.JclCommandStatementContext) {
    }

    override fun enterScheduleStatement(ctx: JCLPPParser.ScheduleStatementContext) {
    }

    override fun enterNotifyStatement(ctx: JCLPPParser.NotifyStatementContext) {
    }

    override fun enterOutputStatement(ctx: JCLPPParser.OutputStatementContext) {
    }

    override fun enterXmitStatement(ctx: JCLPPParser.XmitStatementContext) {}

    override fun enterSetOperation(ctx: JCLPPParser.SetOperationContext) {

    }

    override fun enterProcStatement(ctx: JCLPPParser.ProcStatementContext) {

    }

    override fun enterDefineSymbolicParameter(ctx: JCLPPParser.DefineSymbolicParameterContext) {

    }

    override fun enterPendStatement(ctx: JCLPPParser.PendStatementContext) {

    }

    override fun enterIncludeStatement(ctx: JCLPPParser.IncludeStatementContext) {

    }

    override fun enterJclStep(ctx: JCLPPParser.JclStepContext) {

    }

    override fun enterErrorChars(ctx: JCLPPParser.ErrorCharsContext) {

    }

    override fun exitStartRule(ctx: JCLPPParser.StartRuleContext) {

    }
}
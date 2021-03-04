package io.opentelemetry.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.linecorp.armeria.client.Clients;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.SeverityNumber;

public class OtlpAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private LogsServiceGrpc.LogsServiceBlockingStub client;
  private String url = "http://127.0.0.1:4317/";

  @Override
  public void start() {
    client = Clients.newClient("gproto+" + url, LogsServiceGrpc.LogsServiceBlockingStub.class);
    addFilter(new Filter<>() {
      @Override
      public FilterReply decide(ILoggingEvent event) {
        return (event.getLoggerName().startsWith("com.linecorp") || event.getLoggerName().startsWith("io.netty")) ? FilterReply.DENY : FilterReply.NEUTRAL;
      }
    });
    super.start();
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    if (!isStarted()) {
      return;
    }

    var req = ExportLogsServiceRequest
        .newBuilder()
        .addResourceLogs(
            ResourceLogs.newBuilder().
                addInstrumentationLibraryLogs(
                    InstrumentationLibraryLogs.newBuilder()
                        .addLogs(
                            LogRecord
                                .newBuilder()
                                .setName(eventObject.getLoggerName())
                                .setBody(AnyValue.newBuilder().setStringValue(eventObject.getFormattedMessage()))
                                .setSeverityNumber(getSeverity(eventObject.getLevel()))
                        )
                    )
        );
    client.export(req.build());
  }

  private SeverityNumber getSeverity(Level level) {
    if (level.levelInt == Level.ERROR.levelInt) {
      return SeverityNumber.SEVERITY_NUMBER_ERROR;
    }
    if (level.levelInt == Level.WARN.levelInt) {
      return SeverityNumber.SEVERITY_NUMBER_WARN;
    }
    if (level.levelInt == Level.INFO.levelInt) {
      return SeverityNumber.SEVERITY_NUMBER_INFO;
    }
    if (level.levelInt == Level.DEBUG.levelInt) {
      return SeverityNumber.SEVERITY_NUMBER_DEBUG;
    }
    if (level.levelInt == Level.TRACE.levelInt) {
      return SeverityNumber.SEVERITY_NUMBER_TRACE;
    }
    return SeverityNumber.UNRECOGNIZED;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}

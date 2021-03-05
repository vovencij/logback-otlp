# logback-otlp
OTLP Appender for Logback

## Disclaimer
This an experimental project, do not use in production.

## Usage

```xml
<configuration>
    <appender name="OTLP" class="io.opentelemetry.logging.logback.OtlpAppender">
        <url>http://127.0.0.1:4317/</url>
    </appender>

    <root level="debug">
        <appender-ref ref="OTLP" />
    </root>
</configuration>
```
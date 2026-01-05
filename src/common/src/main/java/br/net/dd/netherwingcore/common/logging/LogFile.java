package br.net.dd.netherwingcore.common.logging;

import java.nio.file.Path;

public record LogFile(Path... paths) implements Detail {
}

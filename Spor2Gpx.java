import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class Spor2Gpx {
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static class E implements AutoCloseable {
        private final XMLStreamWriter xml;

        E(XMLStreamWriter xml, String name) throws XMLStreamException {
            this.xml = xml;
            xml.writeStartElement(name);
        }

        @Override
        public void close() throws XMLStreamException {
            xml.writeEndElement();
        }

        public E attr(String tag, String value) throws XMLStreamException {
            xml.writeAttribute(tag, value);
            return this;
        }

        public E attr(String tag, double value) throws XMLStreamException {
            xml.writeAttribute(tag, String.format("%f", value));
            return this;
        }

        public E ns(String uri) throws XMLStreamException {
            xml.writeDefaultNamespace(uri);
            return this;
        }
    }

    private Spor2Gpx() {
    }

    public static void main(String[] args) throws FactoryConfigurationError, Exception {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(args[0]));
                FileOutputStream fos = new FileOutputStream(args[1])) {

            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(fos);
            xml.writeStartDocument();
            try (@SuppressWarnings("resource") // https://bugs.eclipse.org/bugs/show_bug.cgi?id=436646
            E gpx = new E(xml, "gpx").ns("http://www.topografix.com/GPX/1/0").attr("version", "1.0")
                    .attr("creator", "spor2gpx"); E trkseg = new E(xml, "trkseg")) {
                while (dis.available() > 0) {
                    try (E trkpt = new E(xml, "trkpt").attr("lat", dis.readDouble()).attr("lon",
                            dis.readDouble())) {
                        try (E ele = new E(xml, "ele")) {
                            xml.writeCharacters(String.format("%f", dis.readDouble()));
                        }

                        try (E time = new E(xml, "time")) {
                            xml.writeCharacters(DATE_FMT.format(new Date(dis.readLong())));
                        }
                    }
                }
            } finally {
                xml.writeEndDocument();
                xml.close();
            }
        }
    }
}

package se.raneland.fireball;

import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.local.shared.access.LocalDBClient;
import com.amazonaws.services.dynamodbv2.local.shared.access.sqlite.SQLiteDBAccess;
import com.amazonaws.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.*;

public class Fireball extends LocalDBClient {

	private static final Logger log = getLogger(Fireball.class);

	private static final List<String> NATIVE_LIBRARIES = Collections.unmodifiableList(Arrays.asList(
			"sqlite4java-win32-x64-1.0.392.dll",
			"libsqlite4java-osx-1.0.392.dylib",
			"libsqlite4java-linux-amd64-1.0.392.so"));


	static {
		try {
			// Unpack native libraries
			final Path nativesDir = Files.createTempDirectory("fireball");
			for(String lib : NATIVE_LIBRARIES) {
				final Path libFile = nativesDir.resolve(lib);
				try(InputStream libStream = Fireball.class.getResourceAsStream("/natives/" + lib);
					FileOutputStream fileStream = new FileOutputStream(libFile.toFile())) {
					if(libStream != null) {
						IOUtils.copy(libStream, fileStream);
						log.info("Unpacked {} to {}", lib, libFile);
					} else {
						log.warn("Could not find library {} to unpack", lib);
					}
				} catch(IOException e) {
					log.warn("Could not unpack library {}: {}", lib, e.getMessage(), e);
				}
			}

			// Update library path
			try {
				System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + nativesDir);
				final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			} catch (NoSuchFieldException | IllegalAccessException e) {
				log.error("Could not update library path: {}", e.getMessage(), e);
			}
		} catch (IOException e) {
			log.warn("Could not unpack libraries: {}", e.getMessage(), e);
		}
	}

	private final File databaseFile;
	private final boolean deleteOnShutdown;

	public Fireball() {
		this(new BigInteger(130, new Random()).toString(32), true);
	}

	public Fireball(String filename, boolean deleteOnShutdown) {
		// TODO: Replace with H2 implementation
		super(new SQLiteDBAccess(filename));
		// Schedule removal of the temporary database
		this.databaseFile = new File(filename);
		this.deleteOnShutdown = deleteOnShutdown;
		if(deleteOnShutdown) {
			databaseFile.deleteOnExit();
		}
	}

	@Override
	public void setEndpoint(String endpoint) throws IllegalArgumentException {
		// LocalDBClient throws as error here, we just want to ignore it
	}

	@Override
	public void setRegion(Region region) throws IllegalArgumentException {
		// LocalDBClient throws as error here, we just want to ignore it
	}

	@Override
	public void shutdown() {
		super.shutdown();
		if(deleteOnShutdown) {
			databaseFile.delete();
		}
	}
}

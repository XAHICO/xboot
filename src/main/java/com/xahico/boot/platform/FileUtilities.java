/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.platform;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.io.GenericReadResults;
import com.xahico.boot.io.ReverseFileInputStream;
import com.xahico.boot.pilot.Time;
import com.xahico.boot.util.DateTime;
import com.xahico.boot.util.Enumerator;
import com.xahico.boot.util.Filter;
import com.xahico.boot.util.IOProgressionCallback;
import com.xahico.boot.util.OrderedEnumerator;
import com.xahico.boot.util.StreamUtilities;
import com.xahico.boot.util.StringUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class FileUtilities {
	private static long randomNameGeneratorSalt = System.currentTimeMillis();
	
	
	
	public static String buildExecutableFileName (final String name, final ProcessorArchitecture architecture, final String extension){
		final StringBuilder sb;
		
		assert(null != name);
		
		sb = new StringBuilder();
		sb.append(name.replaceAll("\\s+", ""));
		
		if ((null != architecture) && (architecture != ProcessorArchitecture.UNKNOWN)) {
			if (architecture != ProcessorArchitecture.UNIVERSAL) {
				sb.append("-");
				
				if (architecture.is64Bit()) {
					sb.append("x64");
				} else if (architecture.is32Bit()) {
					sb.append("x86");
				}
			}
		}
		
		if (null != extension) {
			if (! extension.startsWith(".")) {
				sb.append(".");
			}
			
			sb.append(extension);
		}
		
		return sb.toString();
	}
	
	public static File buildFile (final Collection<String> collection){
		File file = null;
		
		for (final String string : collection) {
			if (null != file) 
				file = new File(file, string);
			else {
				file = new File(string);
			}
		}
		
		return file;
	}
	
	public static void copy (final File from, final File to, final CopyOption... options) throws IOException {
		copy(from.toPath(), to.toPath(), options);
	}
	
	public static void copy (final Path fromPath, final Path toPath, final CopyOption... options) throws IOException {
		if (Files.isDirectory(fromPath)) 
			copyDirectory(fromPath.toFile(), toPath.toFile(), options);
		else {
			copyFile(fromPath.toFile(), toPath.toFile(), options);
		}
	}
	
	public static void copyDirectory (final File directory, final File newDirectory, final CopyOption... options) throws IOException {
		copyDirectory(directory.toPath(), newDirectory.toPath(), options);
	}
	
	public static void copyDirectory (final Path directoryPath, final Path newDirectoryPath, final CopyOption... options) throws IOException {
		createDirectories(newDirectoryPath);
		copyDirectoryContents(directoryPath, newDirectoryPath, options);
	}
	
	public static void copyDirectoryContents (final File fromDirectory, final File toDirectory, final CopyOption... options) throws IOException {
		copyDirectoryContents(fromDirectory.toPath(), toDirectory.toPath(), options);
	}
	
	public static void copyDirectoryContents (final Path fromDirectory, final Path toDirectory, final CopyOption... options) throws IOException {
		final AtomicReference<IOException> thrownException;
		
		thrownException = new AtomicReference<>(null);
		
		try (final var tree = Files.walk(fromDirectory, 1)) {
			tree.forEach(path -> {
				if (! path.equals(fromDirectory)) try {
					final File file;
					final File fileInDestination;
					
					file = path.toFile();
					fileInDestination = new File(toDirectory.toFile(), file.getAbsolutePath().substring(fromDirectory.toFile().getAbsolutePath().length()));
					
					copy(file, fileInDestination, options);
				} catch (final IOException ex) {
					thrownException.set(ex);
				}
			});
		}
		
		if (null != thrownException.get()) {
			throw thrownException.get();
		}
	}
	
	public static void copyFile (final File from, final File to, final CopyOption... options) throws IOException {
		Files.copy(from.toPath(), to.toPath(), options);
	}
	
	public static void copyFile (final Path fromPath, final Path toPath, final CopyOption... options) throws IOException {
		Files.copy(fromPath, toPath, options);
	}
	
	public static void copyFile (final InputStream source, final long size, final File file, final int jumpcb, final IOProgressionCallback callback, final CopyOption... options) throws IOException {
		try (final var destination = new FileOutputStream(file)) {
			int  jumps = 0;
			long position = 0;
			
			for (;;) {
				final int b;
				
				b = source.read();

				if (b == -1) {
					break;
				} else {
					destination.write(b);
					
					position++;
					
					if (jumps < jumpcb) 
						jumps++;
					else {
						callback.invoke(position, size);
						
						jumps = 0;
					}
				}
			}
			
			callback.invoke(position, size);
		}
	}
	
	public static void copyTo (final File file, final File directory, final CopyOption... options) throws IOException {
		copyTo(file.toPath(), directory.toPath(), options);
	}
	
	public static void copyTo (final Path filePath, final Path directoryPath, final CopyOption... options) throws IOException {
		final Path newPath;
		
		newPath = Paths.get(directoryPath.toString(), filePath.getFileName().toString());
		
		copy(filePath, newPath, options);
	}
	
	public static void createDirectory (final File file, final FileAttribute<?>... attribs) throws IOException {
		createDirectory(file.toPath(), attribs);
	}
	
	public static void createDirectory (final Path filePath, final FileAttribute<?>... attribs) throws IOException {
		Files.createDirectory(filePath, attribs);
	}
	
	@Helper
	public static void createDirectory (final String filePath, final FileAttribute<?>... attribs) throws IOException {
		createDirectory(Paths.get(filePath), attribs);
	}
	
	public static void createDirectories (final File file, final FileAttribute<?>... attribs) throws IOException {
		createDirectories(file.toPath(), attribs);
	}
	
	public static void createDirectories (final Path filePath, final FileAttribute<?>... attribs) throws IOException {
		Files.createDirectories(filePath, attribs);
	}
	
	@Helper
	public static void createDirectories (final String filePath, final FileAttribute<?>... attribs) throws IOException {
		createDirectories(Paths.get(filePath), attribs);
	}
	
	public static void delete (final File file) throws IOException {
		delete(file.toPath());
	}
	
	public static void delete (final Path filePath) throws IOException {
		if (Files.isDirectory(filePath)) {
			deleteDirectoryContents(filePath);
		}
		
		Files.delete(filePath);
	}
	
	public static void deleteDirectoryContents (final File directory) throws IOException {
		deleteDirectoryContents(directory.toPath());
	}
	
	public static void deleteDirectoryContents (final Path directoryPath) throws IOException {
		final AtomicReference<IOException> thrownException;
		
		thrownException = new AtomicReference<>(null);
		
		try (final var tree = Files.walk(directoryPath, 1)) {
			tree.forEach(path -> {
				if (! path.equals(directoryPath)) try {
					delete(path);
				} catch (final IOException ex) {
					thrownException.set(ex);
				}
			});
		}
		
		if (null != thrownException.get()) {
			throw thrownException.get();
		}
	}
	
	public static boolean fileNameHasExtension (final String fileName){
		final File file;
		final int  position;
		
		file = new File(fileName);
		
		position = file.getName().indexOf(".");
		
		return (position >= 0);
	}
	
	public static String generateRandomFileName (final int length, final String extension){
		final Random        random;
		final StringBuilder sb;
		
		random = new Random();
		random.setSeed(randomNameGeneratorSalt);
		
		randomNameGeneratorSalt++;
		
		sb = new StringBuilder();
		
		while (sb.length() < length) {
			final char c;
			
			c = (char) random.nextInt(Byte.MAX_VALUE);
			
			if (sb.length() > 0) {
				if (Character.isDigit(c)) {
					sb.append(c);
					
					continue;
				}
			}
			
			if (Character.isAlphabetic(c)) {
				sb.append(c);
			}
		}
		
		if (null != extension) {
			if (! extension.startsWith(".")) 
				sb.append(".");
			
			sb.append(extension);
		}
		
		return sb.toString();
	}
	
	public static File getFile (final File root, final String... names){
		File file;
		
		file = root;
		
		for (final var name : names) {
			file = new File(file, name);
		}
		
		return file;
	}
	
	public static String getFileExtension (final File file){
		return getFileExtension(file.getName());
	}
	
	public static String getFileExtension (final File file, final int count){
		return getFileExtension(file.getName(), count);
	}
	
	public static String getFileExtension (final Path filePath){
		return getFileExtension(filePath.toFile());
	}
	
	public static String getFileExtension (final Path filePath, final int count){
		return getFileExtension(filePath.toFile(), count);
	}
	
	public static String getFileExtension (final String fileName){
		final int delimiter;
		
		delimiter = fileName.indexOf('.');
		
		if (delimiter == -1) 
			return null;
		
		return ("." + fileName.substring(delimiter + 1));
	}
	
	public static String getFileExtension (final String fileName, final int count){
		final Iterator<String> it;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		
		it = StringUtilities.splitStringIntoIterator(fileName, ".", false);
		
		for (var i = 0; i <= count; i++) {
			final String fileExtension;
			
			if (! it.hasNext()) 
				break;
			
			fileExtension = it.next();
			
			if (i > 0) {
				sb.append(".");
				sb.append(fileExtension);
			}
		}
		
		return sb.toString();
	}
	
	public static File getFileInDirectory (final File directory, final String lookupFileName) throws FileNotFoundException {
		final AtomicReference<File> atom;
		final Path                  root;
		
		atom = new AtomicReference<>(null);
		
		root = directory.toPath();
		
		try (final var tree = Files.walk(root, 1)) {
			tree.forEach(path -> {
				final File   file;
				final String fileName;
				
				if (null != atom.get()) 
					return;
				
				if (! path.equals(root)) {
					file = path.toFile();
					fileName = file.getName();
					
					if (fileName.equalsIgnoreCase(lookupFileName)) {
						atom.set(file.getAbsoluteFile());
					}
				}
			});
		} catch (final IOException ex) {
			throw new Error(ex);
		}
		
		if (null != atom.get()) 
			return atom.get();
		else {
			throw new FileNotFoundException(String.format("No such file \'%s\'", new File(directory, lookupFileName)));
		}
	}
	
	public static int getFileExtensionLength (final String fileName){
		final int separator;
		
		separator = fileName.indexOf('.');
		
		if (separator == -1) 
			return 0;
		else {
			return (fileName.length() - separator);
		}
	}
	
	public static String getFileNameWithoutExtensions (final File file){
		return getFileNameWithoutExtensions(file.getName());
	}
	
	public static String getFileNameWithoutExtensions (final Path filePath){
		return getFileNameWithoutExtensions(filePath.toFile());
	}
	
	public static String getFileNameWithoutExtensions (final String fileName){
		final int delimiter;
		
		delimiter = fileName.indexOf('.');
		
		if (delimiter == -1) 
			return fileName;
		else {
			return fileName.substring(0, delimiter);
		}
	}
	
	public static DateTime getLastModified (final File file) throws IOException {
		return getLastModified(file.toPath());
	}
	
	public static DateTime getLastModified (final Path filePath) throws IOException {
		final DateTime dateTime;
		final FileTime fileTime;
		
		fileTime = Files.getLastModifiedTime(filePath);
		
		dateTime = new DateTime();
		dateTime.setTimeZone(Time.getLocalTimeZone());
		dateTime.setTime(fileTime.toInstant().atZone(Time.getLocalTimeZone().toZoneId()).toInstant().toEpochMilli());
		
		return dateTime;
	}
	
	public static long getLinesIn (final File file) throws IOException{
		return getLinesIn(file, false);
	}
	
	public static long getLinesIn (final File file, final boolean ignoreBlanks) throws IOException{
		long total = 0;
		
		try (final var reader = new BufferedReader(new FileReader(file))) {
			for (;;) {
				final String line;
				
				line = reader.readLine();
				
				if (null == line) 
					break;
				
				if (ignoreBlanks && line.isBlank()) 
					continue;
				
				total++;
			}
		}
		
		return total;
	}
	
	public static boolean isDescendantOf (final File file, final File supposedParent){
		return file.getAbsolutePath().startsWith(supposedParent.getAbsolutePath());
	}
	
	public static boolean isDescendantOf (final Path filePath, final Path supposedParent){
		return isDescendantOf(filePath.toFile(), supposedParent.toFile());
	}
	
	public static boolean isEmpty (final File file) throws IOException {
		return isEmpty(file.toPath());
	}
	
	public static boolean isEmpty (final Path path) throws IOException {
		if (Files.isDirectory(path)) {
			final AtomicBoolean atom;
			
			atom = new AtomicBoolean(false);
			
			try (final var tree = Files.walk(path, 1)) {
				tree.filter(__ -> !atom.get())
				    .filter(subpath -> !subpath.equals(path))
				    .forEach(__ -> atom.set(true));
			}
			
			return (atom.get() == false);
		} else {
			return (Files.size(path) > 0);
		}
	}
	
	public static File lookup (final File directory, final String lookupFileName, final String lookupFileExtension) throws IOException {
		final AtomicReference<File> atom;
		
		atom = new AtomicReference<>(null);
		
		FileUtilities.walkDirectory(directory, Integer.MAX_VALUE, (file) -> {
			final String fileExtension;
			final String fileName;
			
			if (null != lookupFileExtension) {
				fileExtension = FileUtilities.getFileExtension(file);
				
				if ((null == fileExtension) || !fileExtension.equalsIgnoreCase(lookupFileExtension)) {
					return true;
				}
			}
			
			if (null != lookupFileName) {
				fileName = FileUtilities.getFileNameWithoutExtensions(file);
				
				if ((null == fileName) || !fileName.equalsIgnoreCase(lookupFileName)) {
					return true;
				}
			}
			atom.set(file);
			
			return false;
		});
		
		return atom.get();
	}
	
	public static Path lookup (final Path directoryPath, final String lookupFileName, final String lookupFileExtension) throws IOException {
		return lookup(directoryPath.toFile(), lookupFileName, lookupFileExtension).toPath();
	}
	
	public static void move (final File fromFile, final File toFile, final CopyOption... options) throws IOException {
		move(fromFile.toPath(), toFile.toPath(), options);
	}
	
	public static void move (final Path fromPath, final Path toPath, final CopyOption... options) throws IOException {
		if (Files.isDirectory(fromPath)) {
			if (! Files.exists(fromPath)) {
				Files.createDirectories(fromPath);
			}
			
			moveDirectoryContents(fromPath, toPath, options);
			
			Files.deleteIfExists(fromPath);
		} else {
			Files.createDirectories(toPath.getParent());
			
			Files.move(fromPath, toPath, options);
		}
	}
	
	public static void moveDirectoryContents (final File fromDirectory, final File toDirectory, final CopyOption... options) throws IOException {
		moveDirectoryContents(fromDirectory.toPath(), toDirectory.toPath(), options);
	}
	
	@SuppressWarnings("ThrowFromFinallyBlock")
	public static void moveDirectoryContents (final Path fromPath, final Path toPath, final CopyOption... options) throws IOException {
		try (final var tree = Files.walk(fromPath, 1)) {
			final List<Path>                   moved;
			final AtomicReference<IOException> thrown;
			
			thrown = new AtomicReference<>(null);
			
			moved = new ArrayList<>();
			
			tree.filter(path -> (null == thrown.get())).forEach(path -> {
				if (! path.equals(fromPath)) try {
					moveTo(path, toPath, options);
					
					moved.add(path);
				} catch (final IOException ex) {
					thrown.set(ex);
				}
			});
			
			if (thrown.get() != null) try {
				for (final var path : moved) {
					moveTo(path, fromPath);
				}
			} finally {
				throw thrown.get();
			}
		}
	}
	
	public static File moveTo (final File file, final File directory, final CopyOption... options) throws IOException {
		return moveTo(file.toPath(), directory.toPath(), options).toFile();
	}
	
	public static Path moveTo (final Path filePath, final Path directoryPath, final CopyOption... options) throws IOException {
		final Path newPath;
		
		newPath = Paths.get(directoryPath.toString(), filePath.getFileName().toString());
		
		move(filePath, newPath, options);
		
		return newPath;
	}
	
	public static File pathIn (final File file, final File directory){
		final Deque<String> collection;
		File                superFile;
		
		collection = new LinkedList<>();
		
		superFile = file;
		
		while (null != superFile) {
			if (superFile.equals(directory)) {
				return buildFile(collection);
			}
			
			collection.addFirst(superFile.getName());
			
			superFile = superFile.getParentFile();
		}
		
		return null;
	}
	
	public static Path pathIn (final Path filePath, final Path directoryPath){
		return pathIn(filePath.toFile(), directoryPath.toFile()).toPath();
	}
	
	public static void readAllLines (final File file, final Enumerator<String> handler) throws ExecutionException, IOException {
		readAllLines(file.getPath(), handler);
	}
	
	public static void readAllLines (final Path filePath, final Enumerator<String> handler) throws ExecutionException, IOException {
		readAllLines(filePath.toString(), handler);
	}
	
	public static void readAllLines (final String file, final Enumerator<String> handler) throws ExecutionException, IOException {
		try (final var reader = new BufferedReader(new FileReader(file))) {
			for (;;) try {
				final String line;
				
				line = reader.readLine();
				
				if (null == line) 
					break;
				
				if (line.isBlank()) 
					continue;
				
				handler.accept(line.strip());
			} catch (final Throwable throwable) {
				throw new ExecutionException(throwable);
			}
		}
	}
	
	public static void readAllLines (final File file, final OrderedEnumerator<String> handler) throws IOException {
		readAllLines(file.getPath(), handler);
	}
	
	public static void readAllLines (final Path filePath, final OrderedEnumerator<String> handler) throws IOException {
		readAllLines(filePath.toString(), handler);
	}
	
	public static void readAllLines (final String file, final OrderedEnumerator<String> handler) throws IOException {
		try (final var reader = new BufferedReader(new FileReader(file))) {
			for (;;) {
				final String line;
				
				line = reader.readLine();
				
				if (null == line) 
					break;
				
				if (line.isBlank()) 
					continue;
				
				handler.accept(line.strip());
			}
		}
	}
	
	public static void readAllLinesExtended (final File file, final long position, final long count, final boolean reversed, final Consumer<String> handler) throws IOException {
		final AtomicLong atom;
		
		atom = new AtomicLong(0);
		
		if (reversed) {
			readAllLinesReversed(file, (line) -> {
				final long index;
				
				index = atom.getAndIncrement();
				
				if (index < position) 
					return true;
				
				if (index >= (position + count)) {
					return false;
				}
				
				handler.accept(line);
				
				return true;
			});
		} else {
			readAllLines(file, (line) -> {
				final long index;
				
				index = atom.getAndIncrement();
				
				if (index < position) 
					return true;
				
				if (index >= (position + count)) {
					return false;
				}
				
				handler.accept(line);
				
				return true;
			});
		}
	}
	
	public static void readAllLinesReversed (final File file, final OrderedEnumerator<String> handler) throws IOException {
		try (final var reader = new BufferedReader(new InputStreamReader(new ReverseFileInputStream(file)))) {
			for (;;) {
				final String line;
				
				line = reader.readLine();
				
				if (null == line) 
					break;
				
				if (line.length() <= 1) 
					continue;
				
				if (line.isBlank()) 
					continue;
				
				if (! handler.accept(StringUtilities.reverse(line))) {
					break;
				}
			}
		}
	}
	
	public static void readAllLinesReversed (final Path filePath, final OrderedEnumerator<String> handler) throws IOException {
		readAllLinesReversed(filePath.toFile(), handler);
	}
	
	public static void readAllSliced (final File file, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		readAllSliced(file.getPath(), separator, ignoreCase, handler);
	}
	
	public static void readAllSliced (final Path filePath, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		readAllSliced(filePath.toString(), separator, ignoreCase, handler);
	}
	
	public static void readAllSliced (final String file, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		try (final var stream = new InputStreamReader(new FileInputStream(file), UTF_8)) {
			StreamUtilities.readAllBytesWith(stream, separator, ignoreCase, handler);
		}
	}
	
	public static void readAllSliced (final File file, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		readAllSliced(file.getPath(), separator, ignoreCase, handler);
	}
	
	public static void readAllSliced (final Path filePath, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		readAllSliced(filePath.toString(), separator, ignoreCase, handler);
	}
	
	public static void readAllSliced (final String file, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		try (final var stream = new InputStreamReader(new FileInputStream(file), UTF_8)) {
			StreamUtilities.readAllBytesWith(stream, separator, ignoreCase, handler);
		}
	}
	
	public static String readBinaryToHexString (final File file) throws IOException {
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		try (final var stream = new FileInputStream(file)) {
			for (;;) {
				final int b;
				
				b = stream.read();
				
				if (b == -1) {
					break;
				}
				
				sb.append('\\');
				sb.append('x');
				sb.append(Integer.toHexString(b));
			}
		}
		
		return sb.toString();
	}
	
	public static String readBinaryToHexString (final Path filePath) throws IOException {
		return readBinaryToHexString(filePath.toFile());
	}
	
	public static String readGenericString (final File file) throws IOException {
		return readGenericString(file.toPath());
	}
	
	public static String readGenericString (final Path filePath) throws IOException {
		return Files.readString(filePath);
	}
	
	public static GenericReadResults readLines (final File file, final long position, final OrderedEnumerator<String> handler) throws IOException {
		try (final var stream = new FileInputStream(file)) {
			long                     cursor = 0;
			final GenericReadResults results;
			final StringBuilder      sb;
			
			results = new GenericReadResults();
			
			if (position > 0) {
				stream.skipNBytes(position);
				
				cursor = position;
			}
			
			sb = new StringBuilder();
			
			for (;;) {
				final int  b;
				final char c;
				
				b = stream.read();
				
				if (b == -1) {
					if (! sb.isEmpty()) {
						handler.accept(sb.toString());
					}
					
					results.markEOF(true);
					
					break;
				}
				
				cursor++;
				
				c = (char)(b);
				
				if (c == '\n') {
					if (! handler.accept(sb.toString())) {
						break;
					}
					
					sb.delete(0, sb.length());
				} else {
					sb.append(c);
				}
			}
			
			results.cursor(cursor);
			
			return results;
		}
	}
	
	public static GenericReadResults readLinesExtended (final File file, final long position, final boolean reversed, final OrderedEnumerator<String> handler) throws IOException {
		return (reversed ? readLinesReversed(file, position, handler) : readLines(file, position, handler));
	}
	
	public static GenericReadResults readLinesReversed (final File file, final long position, final OrderedEnumerator<String> handler) throws IOException {
		try (final var stream = new ReverseFileInputStream(file)) {
			long                     cursor = 0;
			final GenericReadResults results;
			final StringBuilder      sb;
			
			results = new GenericReadResults();
			
			stream.position(position);
			
			sb = new StringBuilder();
			
			for (;;) {
				final int  b;
				final char c;
				
				b = stream.read();
				
				if (b == -1) {
					if (! sb.isEmpty()) {
						handler.accept(sb.toString());
					}
					
					results.markEOF(true);
					
					break;
				}
				
				cursor++;
				
				c = (char)(b);
				
				if (c == '\n') {
					if (! handler.accept(sb.toString())) {
						break;
					}
					
					sb.delete(0, sb.length());
				} else {
					sb.insert(0, c);
				}
			}
			
			if (position > 0) 
				results.cursor((stream.total() - cursor) - (stream.total() - position));
			else {
				results.cursor(stream.total() - cursor);
			}
			
			return results;
		}
	}
	
	public static File reassignParent (final File file, final File oldParent, final File newParent){
		return new File(newParent, removeParent(file, oldParent).getPath());
	}
	
	public static File removeExtensions (final File file){
		if (null != file.getParentFile()) {
			return new File(file.getParentFile(), FileUtilities.getFileNameWithoutExtensions(file));
		} else {
			return new File(FileUtilities.getFileNameWithoutExtensions(file));
		}
	}
	
	public static File removeParent (final File file, final File target){
		final File parent;
		
		parent = file.getParentFile();
		
		if (null == parent) 
			return file;
		else {
			return new File(file.getPath().substring(target.getPath().length() + 1));
		}
	}
	
	public static void walkDirectory (final File directory, final int maxDepth, final Enumerator<File> callback) throws ExecutionException, IOException {
		walkDirectory(directory.toPath(), maxDepth, (path) -> {
			callback.accept(path.toFile());
		});
	}
	
	public static void walkDirectory (final Path directoryPath, final int maxDepth, final Enumerator<Path> callback) throws ExecutionException, IOException {
		final AtomicReference<Throwable> thrownException;
		
		thrownException = new AtomicReference<>(null);
		
		try (final var stream = Files.walk(directoryPath, maxDepth)) {
			stream.filter(__ -> (thrownException.get() == null)).filter(path -> !path.equals(directoryPath)).forEach(path -> {
				try {
					callback.accept(path);
				} catch (final Throwable ex) {
					thrownException.set(ex);
				}
			});
		}
		
		if (null != thrownException.get()) {
			throw new ExecutionException(thrownException.get());
		}
	}
	
	public static void walkDirectory (final File directory, final int maxDepth, final OrderedEnumerator<File> callback) throws IOException {
		walkDirectory(directory.toPath(), maxDepth, (path) -> {
			return callback.accept(path.toFile());
		});
	}
	
	public static void walkDirectory (final Path directoryPath, final int maxDepth, final OrderedEnumerator<Path> callback) throws IOException {
		final AtomicBoolean atom;
		
		atom = new AtomicBoolean(false);
		
		try (final var stream = Files.walk(directoryPath, maxDepth)) {
			stream.filter(__ -> (atom.get() == false)).filter(path -> !path.equals(directoryPath)).forEach(path -> {
				if (! callback.accept(path)) {
					atom.set(true);
				}
			});
		}
	}
	
	public static void walkDirectoryRaw (final File directory, final int maxDepth, final Consumer<File> callback) throws IOException {
		walkDirectoryRaw(directory.toPath(), maxDepth, (path) -> {
			callback.accept(path.toFile());
		});
	}
	
	public static void walkDirectoryRaw (final Path directoryPath, final int maxDepth, final Consumer<Path> callback) throws IOException {
		try (final var stream = Files.walk(directoryPath, maxDepth)) {
			stream.filter(path -> !path.equals(directoryPath)).forEach(path -> callback.accept(path));
		}
	}
	
	public static long weighDirectory (final File directory) throws IOException {
		return weighDirectory(directory.toPath());
	}
	
	public static long weighDirectory (final Path directoryPath) throws IOException {
		try {
			final AtomicLong atom;
			
			atom = new AtomicLong();
			
			FileUtilities.walkDirectory(directoryPath, Integer.MAX_VALUE, (path) -> {
				atom.addAndGet(weighFile(path));
			});
			
			return atom.get();
		} catch (final ExecutionException ex) {
			if (ex.getCause() instanceof IOException) 
				throw (IOException)(ex.getCause());
			else {
				throw new IOException(ex.getCause());
			}
		}
	}
	
	public static long weighFile (final File file) throws IOException {
		return weighFile(file.toPath());
	}
	
	public static long weighFile (final Path filePath) throws IOException {
		return Files.size(filePath);
	}
	
	public static void writeList (final File file, final List<String> list) throws IOException {
		try (final var stream = new FileOutputStream(file)) {
			for (final var string : list) {
				stream.write(string.getBytes());
				stream.write("\n".getBytes());
			}
		}
	}
	
	public static void writeList (final Path filePath, final List<String> list) throws IOException {
		writeList(filePath.toFile(), list);
	}
	
	public static void writeList (final File file, final List<String> list, final Filter<String> filter, final Charset charset) throws IOException {
		try (final var stream = new FileOutputStream(file)) {
			for (final var string : list) {
				if (filter.accept(string)) {
					stream.write(string.getBytes(charset));
					stream.write("\n".getBytes(charset));
				}
			}
		}
	}
	
	
	
	private FileUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}
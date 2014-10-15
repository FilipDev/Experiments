package memoryfile;

import java.util.Collection;
import java.util.HashMap;

public class MemoryFolder {

	String path;
	HashMap<String, MemoryFile> files = new HashMap<>();

	public MemoryFolder(String path)
	{
		this.path = path;
	}

	public void addFile(MemoryFile file)
	{
		this.files.put(file.getFile().getName(), file);
	}

	public void removeFile(MemoryFile file)
	{
		this.files.remove(file.getFile().getName());
	}

	public String getPath()
	{
		return this.path;
	}

	public Collection<MemoryFile> getFiles()
	{
		return files.values();
	}

	@Override
	public String toString()
	{
		return this.files.toString();
	}
}


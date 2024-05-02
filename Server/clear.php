<?php
delete_files("Saved/");
function delete_files($target)
{
	if(is_dir($target))
	{
		$files = glob($target.'*', GLOB_MARK);
		foreach($files as $file)
		{
			delete_files($file);
		}
		rmdir($target);
	}
	elseif(is_file($target))
	{
		unlink($target);
	}
}
?>
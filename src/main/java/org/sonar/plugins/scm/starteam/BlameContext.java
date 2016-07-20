package org.sonar.plugins.scm.starteam;

import java.io.File;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.scm.BlameLine;

import com.starteam.ViewMember;

public class BlameContext
{
  private List<BlameLine> blameLines;
  private ViewMember previous = null;
  private ViewMember current = null;  
  private File previousFile = null;
  private File currentFile = null;
  private InputFile inputFile = null;
  private boolean needBlame = false;
  private boolean lastRecord=false;
  private com.starteam.File stFile;
  private int expectedLines;
  
  public BlameContext(List<BlameLine> blameLines,InputFile inputFile,com.starteam.File stFile,int expectedLines){
    this.blameLines=blameLines;
    this.inputFile=inputFile;
    this.stFile=stFile;
    this.expectedLines=expectedLines;
  }

  public List<BlameLine> getBlameLines()
  {
    return blameLines;
  }

  public void setBlameLines(List<BlameLine> blameLines)
  {
    this.blameLines = blameLines;
  }

  public ViewMember getPrevious()
  {
    return previous;
  }

  public void setPrevious(ViewMember previous)
  {
    this.previous = previous;
  }

  public ViewMember getCurrent()
  {
    return current;
  }

  public void setCurrent(ViewMember current)
  {
    this.previous=this.current;
    this.current = current;
    this.needBlame=true;
  }

  public File getPreviousFile()
  {
    return previousFile;
  }

  public void setPreviousFile(File previousFile)
  {
    this.previousFile = previousFile;
  }

  public File getCurrentFile()
  {
    return currentFile;
  }

  public void setCurrentFile(File currentFile)
  {
    if(this.previousFile!=null&&this.previousFile.exists()){
      this.previousFile.delete();
    }
    this.previousFile=this.currentFile;
    this.currentFile = currentFile;
    this.needBlame=true;
  }

  public InputFile getInputFile()
  {
    return inputFile;
  }

  public void setInputFile(InputFile inputFile)
  {
    this.inputFile = inputFile;
  }

  public boolean isNeedBlame()
  {
    return needBlame;
  }
  public void blamed()
  {
    this.needBlame=false;
    if(isLastRecord()){
      
      if(this.previousFile!=null&&this.previousFile.exists()){
        this.previousFile.delete();
      }
      if(this.currentFile!=null&&this.currentFile.exists()){
        this.currentFile.delete();
        currentFile.getParentFile().delete();
      }      
    }
  }
  public boolean isLastRecord()
  {
    return lastRecord;
  }

  public void setLastRecord(boolean lastRecord)
  {
    this.lastRecord = lastRecord;
  }

  public com.starteam.File getStFile()
  {
    return stFile;
  }

  public void setStFile(com.starteam.File stFile)
  {
    this.stFile = stFile;
  }

  public int getExpectedLines()
  {
    return expectedLines;
  }

  public void setExpectedLines(int expectedLines)
  {
    this.expectedLines = expectedLines;
  }  
  
  
}

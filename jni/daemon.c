#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>

int daemon_init(void) 
{ 
	pid_t pid; 
  if((pid = fork()) < 0) 
  	return(-1); 
  else if(pid != 0) 
  {
      __android_log_write(ANDROID_LOG_ERROR,"daemon","daemon parent got killed");   
      exit(0); /* parent exit */ 
  }
  return(0); 
}
 
int Java_com_rich_service_RegisterSMSService_daemon(void) 
{ 
    int id=0;
	if((id=daemon_init()) == -1) 
  { 
  	exit(0); 
  } 
    __android_log_print(ANDROID_LOG_ERROR,"daemon","daemon %d running",id);
  while(1) 
  { 
  	sleep(1);
  } 
  return(0); 
}
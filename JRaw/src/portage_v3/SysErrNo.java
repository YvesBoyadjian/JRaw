/*
 * SysErrNo.java
 *
 * Created on 12 d�cembre 2005, 13:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

/**
 *
 * @author w22w087
 */
public class SysErrNo {
    /*
 * Error codes
 */

public static final int
        EPERM	=1,	/* Not super-user			*/
   	ENOENT	=2,	/* No such file or directory		*/
   	ESRCH	=3,	/* No such process			*/
   	EINTR	=4,	/* interrupted system call		*/
   	EIO	=5,	/* I/O error				*/
   	ENXIO	=6,	/* No such device or address		*/
   	E2BIG	=7,	/* Arg list too long			*/
   	ENOEXEC	=8,	/* Exec format error			*/
   	EBADF	=9,	/* Bad file number			*/
   	ECHILD	=10,	/* No children				*/
   	EAGAIN	=11,	/* Resource temporarily unavailable	*/
   	ENOMEM	=12,	/* Not enough core			*/
   	EACCES	=13,	/* Permission denied			*/
   	EFAULT	=14,	/* Bad address				*/
   	ENOTBLK	=15,	/* Block device required		*/
   	EBUSY	=16,	/* Mount device busy			*/
   	EEXIST	=17,	/* File exists				*/
   	EXDEV	=18,	/* Cross-device link			*/
   	ENODEV	=19,	/* No such device			*/
   	ENOTDIR	=20,	/* Not a directory			*/
   	EISDIR	=21,	/* Is a directory			*/
   	EINVAL	=22,	/* Invalid argument			*/
   	ENFILE	=23,	/* File table overflow			*/
   	EMFILE	=24,	/* Too many open files			*/
   	ENOTTY	=25,	/* Inappropriate ioctl for device	*/
   	ETXTBSY	=26,	/* Text file busy			*/
   	EFBIG	=27,	/* File too large			*/
   	ENOSPC	=28,	/* No space left on device		*/
   	ESPIPE	=29,	/* Illegal seek				*/
   	EROFS	=30,	/* Read only file system		*/
   	EMLINK	=31,	/* Too many links			*/
   	EPIPE	=32,	/* Broken pipe				*/
   	EDOM	=33,	/* Math arg out of domain of func	*/
   	ERANGE	=34,	/* Math result not representable	*/
   	ENOMSG	=35,	/* No message of desired type		*/
   	EIDRM	=36,	/* Identifier removed			*/
   	ECHRNG	=37,	/* Channel number out of range		*/
   	EL2NSYNC= 38,	/* Level 2 not synchronized		*/
   	EL3HLT	=39,	/* Level 3 halted			*/
   	EL3RST	=40,	/* Level 3 reset			*/
   	ELNRNG	=41,	/* Link number out of range		*/
   	EUNATCH =42,	/* Protocol driver not attached		*/
   	ENOCSI	=43,	/* No CSI structure available		*/
   	EL2HLT	=44,	/* Level 2 halted			*/
   	EDEADLK	=45,	/* Deadlock condition.			*/
   	ENOLCK	=46,	/* No record locks available.		*/
   	ECANCELED =47,	/* Operation canceled			*/
   	ENOTSUP	=48,	/* Operation not supported		*/

/* Filesystem Quotas */
   	EDQUOT	=49,	/* Disc quota exceeded			*/

/* Convergent Error Returns */
   	EBADE	=50,	/* invalid exchange			*/
   	EBADR	=51,	/* invalid request descriptor		*/
   	EXFULL	=52,	/* exchange full			*/
   	ENOANO	=53,	/* no anode				*/
   	EBADRQC	=54,	/* invalid request code			*/
   	EBADSLT	=55,	/* invalid slot				*/
   	EDEADLOCK =56,	/* file locking deadlock error		*/

   	EBFONT	=57,	/* bad font file fmt			*/

/* Interprocess Robust Locks */
   	EOWNERDEAD	=58,	/* process died with the lock */
   	ENOTRECOVERABLE	=59,	/* lock is not recoverable */

/* stream problems */
   	ENOSTR	=60,	/* Device not a stream			*/
   	ENODATA	=61,	/* no data (for no delay io)		*/
   	ETIME	=62,	/* timer expired			*/
   	ENOSR	=63,	/* out of streams resources		*/

   	ENONET	=64,	/* Machine is not on the network	*/
   	ENOPKG	=65,	/* Package not installed		*/
   	EREMOTE	=66,	/* The object is remote			*/
   	ENOLINK	=67,	/* the link has been severed		*/
   	EADV	=68,	/* advertise error			*/
   	ESRMNT	=69,	/* srmount error			*/

   	ECOMM	=70,	/* Communication error on send		*/
   	EPROTO	=71,	/* Protocol error			*/

/* Interprocess Robust Locks */
   	ELOCKUNMAPPED	=72,	/* locked lock was unmapped */

   	EMULTIHOP =74,	/* multihop attempted			*/
   	EBADMSG =77,	/* trying to read unreadable message	*/
   	ENAMETOOLONG =78,/* path name is too long		*/
   	EOVERFLOW =79,	/* value too large to be stored in data type */
   	ENOTUNIQ =80,	/* given log. name not unique		*/
   	EBADFD	=81,	/* f.d. invalid for this operation	*/
   	EREMCHG	=82,	/* Remote address changed		*/

/* shared library problems */
   	ELIBACC	=83,	/* Can't access a needed shared lib.	*/
   	ELIBBAD	=84,	/* Accessing a corrupted shared lib.	*/
   	ELIBSCN	=85,	/* .lib section in a.out corrupted.	*/
   	ELIBMAX	=86,	/* Attempting to link in too many libs.	*/
   	ELIBEXEC= 87,	/* Attempting to exec a shared library.	*/
   	EILSEQ	=88,	/* Illegal byte sequence.		*/
   	ENOSYS	=89,	/* Unsupported file system operation	*/
   	ELOOP	=90,	/* Symbolic link loop			*/
   	ERESTART= 91,	/* Restartable system call		*/
   	ESTRPIPE= 92,	/* if pipe/FIFO, don't sleep in stream head */
   	ENOTEMPTY =93,	/* directory not empty			*/
   	EUSERS	=94,	/* Too many users (for UFS)		*/

/* BSD Networking Software */
	/* argument errors */
   	ENOTSOCK	=95,	/* Socket operation on non-socket */
   	EDESTADDRREQ	=96,	/* Destination address required */
   	EMSGSIZE	=97,	/* Message too long */
   	EPROTOTYPE	=98,	/* Protocol wrong type for socket */
   	ENOPROTOOPT	=99,	/* Protocol not available */
   	EPROTONOSUPPORT	=120,	/* Protocol not supported */
   	ESOCKTNOSUPPORT	=121,	/* Socket type not supported */
   	EOPNOTSUPP	=122,	/* Operation not supported on socket */
   	EPFNOSUPPORT	=123,	/* Protocol family not supported */
   	EAFNOSUPPORT	=124,	/* Address family not supported by */
				/* protocol family */
   	EADDRINUSE	=125,	/* Address already in use */
   	EADDRNOTAVAIL	=126,	/* Can't assign requested address */
	/* operational errors */
   	ENETDOWN	=127,	/* Network is down */
   	ENETUNREACH	=128,	/* Network is unreachable */
   	ENETRESET	=129,	/* Network dropped connection because */
				/* of reset */
   	ECONNABORTED	=130,	/* Software caused connection abort */
   	ECONNRESET	=131,	/* Connection reset by peer */
   	ENOBUFS		=132,	/* No buffer space available */
   	EISCONN		=133,	/* Socket is already connected */
   	ENOTCONN	=134,	/* Socket is not connected */
/* XENIX has 135 - 142 */
   	ESHUTDOWN	=143,	/* Can't send after socket shutdown */
   	ETOOMANYREFS	=144,	/* Too many references: can't splice */
   	ETIMEDOUT	=145,	/* Connection timed out */
   	ECONNREFUSED	=146,	/* Connection refused */
   	EHOSTDOWN	=147,	/* Host is down */
   	EHOSTUNREACH	=148,	/* No route to host */
   	EWOULDBLOCK	=EAGAIN,
   	EALREADY	=149,	/* operation already in progress */
   	EINPROGRESS	=150,	/* operation now in progress */

/* SUN Network File System */
   	ESTALE		=151;	/* Stale NFS file handle */


    /** Creates a new instance of SysErrNo */
    public SysErrNo() {
    }
    
}

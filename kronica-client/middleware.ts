import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// This function can be marked `async` if using `await` inside
export function middleware(request: NextRequest) {
  // if (request.nextUrl.pathname.startsWith('/login')) {
  //   // const isLoggedIn = request.cookies.has("JSESSIONID")
  //   // // const fuck = 'fuck'
  //   // // return NextResponse.redirect(new URL('/', request.url))
  //   // // return NextResponse.next()
  //   // if (isLoggedIn) {
  //   //   return NextResponse.redirect(new URL('/', request.url))
  //   // }
  // }
}
export const config = {
  matcher: '/:path*',
}
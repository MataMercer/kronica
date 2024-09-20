import Link from "next/link";
import { useRouter } from "next/navigation";
import { cookies } from "next/headers";
import LogoutButton from "./LogoutButton";
import { fetchCurrentUser } from "./fetch/auth";

export default async function Navbar() {
    const currentUser = await fetchCurrentUser();
    return (
        <nav className="flex text-2xl min-h-[5vh]  pt-5 bg-black text-white">
            <ul className="flex space-x-5 pt-3 ml-10">
                <li>
                    <Link href="/">HOME</Link>
                </li>

                {!currentUser.id ? (
                    <li>
                        <Link href="/login">LOGIN</Link>
                    </li>
                ) : (
                    <>
                        <li>
                            <Link href={`/users/${currentUser.id}`}>
                                🟢{currentUser.name.toUpperCase()}
                            </Link>
                        </li>
                        <li>
                            <LogoutButton />
                        </li>
                    </>
                )}
            </ul>
        </nav>
    );
}

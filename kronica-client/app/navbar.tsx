import Link from "next/link";
import { useRouter } from "next/navigation";
import { cookies } from "next/headers";
import LogoutButton from "./LogoutButton";
import { fetchCurrentUser } from "./fetch/auth";

export default async function Navbar() {
    const currentUser = await fetchCurrentUser();
    return (
        <nav className="flex text-2xl min-h-[5vh]  pt-5 border-black border-solid border-b-[1px]">
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
                            <Link href="/user">
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

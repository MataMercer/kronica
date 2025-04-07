"use client";
import Link from "next/link";
import LogoutButton from "./LogoutButton";
import Image from "next/image";
import useCurrentUser from "./hooks/useCurrentUser";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { DropdownMenuTrigger } from "@radix-ui/react-dropdown-menu";
import NotificationIndicator from "./NotificationIndicator";
import useNotifications from "./hooks/useNotifications";

export default function Navbar() {
    const {
        user: currentUser,
        loading,
        loggedOut,
        mutate: mutateCurrentUser,
    } = useCurrentUser();

    const { notifications, mutate: mutateNotifications } = useNotifications();

    return (
        <nav className="fixed w-[100%] flex text-2xl min-h-[60px] bg-black text-white">
            <Link className="ml-10" href="/">
                <Image
                    alt="website logo"
                    src="/logo.png"
                    width={150}
                    height={75}
                />
            </Link>
            <ul className="flex justify-end space-x-5 pt-3 ml-10">
                {currentUser && currentUser.id ? (
                    <>
                        <DropdownMenu>
                            <DropdownMenuTrigger
                                onClick={() => {
                                    mutateNotifications();
                                }}
                            >
                                <NotificationIndicator />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent>
                                {notifications?.map((n) => (
                                    <DropdownMenuItem key={n.id}>
                                        {n.message}
                                        <div></div>
                                    </DropdownMenuItem>
                                ))}
                            </DropdownMenuContent>
                        </DropdownMenu>

                        <DropdownMenu>
                            <DropdownMenuTrigger>
                                @{currentUser.name.toUpperCase()}
                            </DropdownMenuTrigger>
                            <DropdownMenuContent>
                                <DropdownMenuItem className="">
                                    <Link href={`/users/${currentUser.id}`}>
                                        PROFILE
                                    </Link>
                                </DropdownMenuItem>
                                <DropdownMenuItem>
                                    <LogoutButton />
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </>
                ) : (
                    <li className="text-center">
                        <Link href="/login">LOGIN</Link>
                    </li>
                )}
            </ul>
        </nav>
    );
}

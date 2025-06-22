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
import { Search } from "lucide-react";
import Alert from "@/components/CustomUi/Alert";

export default function Navbar() {
    const {
        user: currentUser,
        loading,
        loggedOut,
        mutate: mutateCurrentUser,
    } = useCurrentUser();

    const { notifications, mutate: mutateNotifications } = useNotifications();

    return (
        <nav className="fixed w-[70vw] flex text-2xl min-h-[60px] justify-between bg-white border-b-[1px] border-black ">
            <Link className="ml-10" href="/">
                <Image
                    alt="website logo"
                    src="/logo.png"
                    width={150}
                    height={75}
                />
            </Link>
            <div>
                <input className="bg-white m-2" />
                <button>
                    <Search />
                </button>
            </div>

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
                                <Link href={`/users/${currentUser.id}`}>
                                    <DropdownMenuItem className="">
                                        PROFILE
                                    </DropdownMenuItem>
                                </Link>
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

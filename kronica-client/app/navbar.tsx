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
import { ChevronDown, Search } from "lucide-react";
import Alert from "@/components/CustomUi/Alert";
import AuthProtection from "./auth/AuthProtection";

export default function Navbar() {
    const {
        user: currentUser,
        loading,
        loggedOut,
        mutate: mutateCurrentUser,
    } = useCurrentUser();

    const { notifications, mutate: mutateNotifications } = useNotifications();

    const websiteName = "KRONIKA";
    return (
        <nav className="fixed w-[70vw] flex text-2xl min-h-[60px] justify-between bg-white border-b-[1px] border-black items-center">
            <Link className="ml-10 flex items-center space-x-2" href="/home">
                <Image
                    alt="website logo"
                    src="/logo.png"
                    width={50}
                    height={50}
                />
                <span className="font-bold">{websiteName}</span>
            </Link>
            <div className="items-center flex">
                <input className="bg-white m-1" />
                <button>
                    <Search />
                </button>
            </div>

            <ul className="flex justify-end space-x-5 ml-10 items-center">
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
                            <DropdownMenuTrigger className="flex space-x-2 items-center ">
                                <Image
                                    className="rounded-full object-fill m-1"
                                    alt="website logo"
                                    src="/placeholder.jpg"
                                    width={40}
                                    height={40}
                                />
                                <span>@{currentUser.name.toUpperCase()}</span>
                                <ChevronDown />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent>
                                <Link href={`/users/${currentUser.id}`}>
                                    <DropdownMenuItem className="">
                                        PROFILE
                                    </DropdownMenuItem>
                                </Link>
                                <Link href={`/users/${currentUser.id}`}>
                                    <DropdownMenuItem className="">
                                        SETTINGS
                                    </DropdownMenuItem>
                                </Link>
                                <AuthProtection requiredRole="ADMIN">
                                    <Link href={`/users/${currentUser.id}`}>
                                        <DropdownMenuItem className="">
                                            ADMINISTRATION
                                        </DropdownMenuItem>
                                    </Link>
                                </AuthProtection>
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

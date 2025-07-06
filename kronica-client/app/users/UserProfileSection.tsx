import { fetchUser } from "@/app/fetch/users";
import { User } from "../Types/Models";
import Badge from "@/components/CustomUi/Badge";
import {BellPlus, Settings, UserPlus} from "lucide-react";
import Image from "next/image";
import { fetchCurrentUser } from "../fetch/auth";

type UserProfileSectionProps = {
    userId: Number;
};
export default async function UserProfileSection({
    userId,
}: UserProfileSectionProps) {
    const user = await fetchUser(userId.toString());
    const currentUser = await fetchCurrentUser();
    return (
        <div className="flex space-x-2  ">
            <Image
                className="rounded-full object-fill"
                alt="website logo"
                src="/placeholder.jpg"
                width={150}
                height={0}
            />
            <div>
                {user && (
                    <div className="text-3xl flex justify-between">
                        <div className="flex space-x-2 ">
                            <span>@{user.name}</span>
                            <Badge>
                                <span className="capitalize">
                                    {user.role
                                        .toLowerCase()
                                        .split("_")
                                        .join(" ")}
                                </span>
                            </Badge>
                        </div>
                        {currentUser.id !== user.id ? (
                            <div className="space-x-2 flex">
                                <button className="button text-xl flex space-x-2 items-center">
                                    <BellPlus />
                                    <span>NOTIFICATIONS</span>
                                </button>
                                <button className="button text-xl flex space-x-2 items-center">
                                    {" "}
                                    <UserPlus />
                                    <span>FOLLOW</span>
                                </button>
                            </div>
                        ): <div className="space-x-2 flex">
                            <button className="button text-xl flex space-x-2 items-center">
                                <Settings/>
                                <span>EDIT PROFILE</span>
                            </button>
                        </div>}
                    </div>
                )}
                <p className="">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    Quisque porttitor luctus mollis. Nunc in ante ut est blandit
                    laoreet. Vivamus ultricies magna eu velit facilisis, nec
                    cursus felis volutpat. Cras.
                </p>
            </div>
        </div>
    );
}

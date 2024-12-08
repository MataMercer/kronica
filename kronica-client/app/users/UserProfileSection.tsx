import { fetchUser, User } from "@/app/fetch/users";

type UserProfileSectionProps = {
    userId: Number;
};
export default async function UserProfileSection({
    userId,
}: UserProfileSectionProps) {
    const user = await fetchUser(userId.toString());
    return (
        <div className="  ">
            {user && <div className="text-3xl">@{user.name}</div>}
            <p className="">
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque
                porttitor luctus mollis. Nunc in ante ut est blandit laoreet.
                Vivamus ultricies magna eu velit facilisis, nec cursus felis
                volutpat. Cras.
            </p>
        </div>
    );
}

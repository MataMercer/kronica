import { User } from "@/app/fetch/users";

type UserProfileSectionProps = {
    user: User;
};
export default function UserProfileSection({ user }: UserProfileSectionProps) {
    return (
        <div>
            {user && (
                <div className="text-3xl border-b-[1px] border-black ">
                    {user.name}
                </div>
            )}
        </div>
    );
}

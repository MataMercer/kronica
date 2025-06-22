import { BadgeCheck } from "lucide-react";

type Props = {
    children?: React.ReactNode;
};

export default function Badge({ children }: Props) {
    return (
        <div className="flex space-x-1 bg-black text-white pt-2 pb-2 px-3 rounded-full text-base">
            <BadgeCheck className="" /> <span> {children}</span>
        </div>
    );
}

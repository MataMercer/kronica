import {CircleSlash2} from "lucide-react";

export default function EmptyPlaceholder() {

    return (
        <div className={"m-2 p-2 flex flex-col justify-center items-center"}>
            <CircleSlash2 size={50}/>
            <h1 className="text-2xl self-center">Nothing to see here...</h1>
        </div>
    )
}
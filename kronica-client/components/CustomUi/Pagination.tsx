import Link from "next/link";

type Props = {
    currentPage?: number;
    totalPages: number;
};

export default function Pagination({ currentPage, totalPages }: Props) {
    const maxVisibleButtons = 8;
    return (
        <div className="flex justify-center items-center space-x-[1px] m-6">
            {currentPage && totalPages > 1 && currentPage > 1 && (
                <Link href={`?page=${currentPage - 1}`}>
                    <button className="button px-4 py-2">PREV</button>
                </Link>
            )}
            {Array.from({ length: totalPages }, (_, index) => (
                <Link href={`?page=${index + 1}`} key={index}>
                    <button
                        className={`button px-4 py-2  ${
                            currentPage === index + 1 ||
                            (index == 0 && !!!currentPage)
                                ? "bg-purple-500 text-white"
                                : " text-black"
                        }`}
                    >
                        {index + 1}
                    </button>
                </Link>
            ))}
            {currentPage && totalPages > 1 && currentPage < totalPages && (
                <Link href={`?page=${currentPage + 1}`}>
                    <button className="button px-4 py-2">NEXT</button>
                </Link>
            )}
        </div>
    );
}
